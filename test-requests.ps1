# 과제 캡처용: 앱(localhost:8080)을 켠 상태에서 PowerShell로 실행
#
#   .\test-requests.ps1 success   성공 응답 (201, 200, 204)
#   .\test-requests.ps1 4xx       클라이언트 오류 (400, 404, 409)
#   .\test-requests.ps1 5xx       서버 오류 (500, 503)
#   .\test-requests.ps1           위 3개를 모두 실행
#
# (실행이 막히면)  powershell -ExecutionPolicy Bypass -File .\test-requests.ps1 success
# 호출마다 요청, 상태 코드, Location 헤더, 응답 본문을 짧게 보여준다.

param([string]$Only = "all")

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$base = "http://localhost:8080/api/v1"
if ($env:API_BASE) { $base = $env:API_BASE }

function Call($title, $method, $path, $body, [switch]$Quiet) {
    $curlArgs = @('-s', '-i', '-X', $method, "$base$path")
    $tmp = $null
    if ($body) {
        $tmp = [System.IO.Path]::GetTempFileName()
        [System.IO.File]::WriteAllText($tmp, $body, (New-Object System.Text.UTF8Encoding($false)))
        $curlArgs += @('-H', 'Content-Type: application/json', '--data-binary', "@$tmp")
    }
    $lines = @(curl.exe @curlArgs)
    if ($tmp) { Remove-Item $tmp -ErrorAction SilentlyContinue }
    if ($Quiet) { return }

    # 응답을 헤더와 본문으로 나눈다 (첫 빈 줄 기준)
    $blank = [array]::IndexOf($lines, "")
    if ($blank -lt 0) { $blank = $lines.Count }
    $headers = $lines[0..([Math]::Max($blank - 1, 0))]
    $resBody = ""
    if ($blank + 1 -lt $lines.Count) { $resBody = ($lines[($blank + 1)..($lines.Count - 1)] -join "") }
    $status = ($headers[0]).Trim()
    $location = $headers | Where-Object { $_ -like 'Location:*' } | Select-Object -First 1

    $color = "Green"
    if ($status -match ' 4\d\d') { $color = "Yellow" }
    if ($status -match ' 5\d\d') { $color = "Red" }

    Write-Host ""
    Write-Host "## $title" -ForegroundColor Cyan
    if ($body) { Write-Host "$method $path   $body" } else { Write-Host "$method $path" }
    Write-Host "=> $status" -ForegroundColor $color
    if ($location) { Write-Host "   $($location.Trim())" }
    if ($resBody) { Write-Host "   $resBody" }
}

function Reset-Data { Call "" DELETE "/items?confirm=true" $null -Quiet }

function Run-Success {
    Reset-Data
    Write-Host "===== 성공 응답 (2xx) =====" -ForegroundColor White
    Call "POST 1/2  생성 (201)"            POST   "/items"         '{"name":"pen","price":1000}'
    Call "POST 2/2  여러 개 생성 (201)"    POST   "/items/bulk"    '[{"name":"note","price":2000},{"name":"eraser","price":500}]'
    Call "GET 1/2  목록 (200)"             GET    "/items"
    Call "GET 2/2  1개 조회 (200)"         GET    "/items/1"
    Call "PUT 1/2  전체 교체 (200)"        PUT    "/items/1"       '{"name":"pencil","price":700}'
    Call "PUT 2/2  가격 교체 (200)"        PUT    "/items/1/price" '{"price":500}'
    Call "DELETE 1/2  1개 삭제 (204)"      DELETE "/items/1"
    Call "DELETE 2/2  전체 삭제 (204)"     DELETE "/items?confirm=true"
}

function Run-4xx {
    Reset-Data
    Call "" POST "/items" '{"name":"pen","price":1000}' -Quiet
    Write-Host "===== 클라이언트 오류 (4xx) =====" -ForegroundColor White
    Call "400  이름이 비었고 가격이 음수"      POST   "/items"       '{"name":"","price":-1}'
    Call "400  PUT 인데 price 없음"            PUT    "/items/1"     '{"name":"only-name"}'
    Call "400  전체 삭제에 confirm 없음"       DELETE "/items"
    Call "404  없는 id 조회"                   GET    "/items/999"
    Call "404  없는 id 삭제"                   DELETE "/items/999"
    Call "409  이름 중복"                      POST   "/items"       '{"name":"pen","price":1}'
    Reset-Data
}

function Run-5xx {
    Write-Host "===== 서버 오류 (5xx) =====" -ForegroundColor White
    Call "500  서버 에러"             GET    "/simulate/500"
    Call "503  서비스 점검"           GET    "/simulate/503"
}

switch ($Only) {
    "success" { Run-Success }
    "4xx"     { Run-4xx }
    "5xx"     { Run-5xx }
    default   { Run-Success; Run-4xx; Run-5xx }
}
Write-Host ""
