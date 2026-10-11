# 과제 캡처용: 앱(localhost:8080)을 켠 상태에서 PowerShell로 실행
#   powershell -ExecutionPolicy Bypass -File .\test-requests.ps1
# 각 호출의 상태 코드, 응답 헤더(Location 등), 본문이 그대로 출력된다.

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$base = "http://localhost:8080/api/v1"

function Call($title, $method, $path, $body) {
    Write-Host ""
    Write-Host "### $title   [$method $path]" -ForegroundColor Cyan
    if ($body) { Write-Host "body: $body" -ForegroundColor DarkGray }
    $curlArgs = @('-s', '-i', '-X', $method, "$base$path")
    $tmp = $null
    if ($body) {
        $tmp = [System.IO.Path]::GetTempFileName()
        [System.IO.File]::WriteAllText($tmp, $body, (New-Object System.Text.UTF8Encoding($false)))
        $curlArgs += @('-H', 'Content-Type: application/json', '--data-binary', "@$tmp")
    }
    curl.exe @curlArgs
    Write-Host ""
    if ($tmp) { Remove-Item $tmp -ErrorAction SilentlyContinue }
}

Call "초기화 (전체 삭제) - DELETE 2/2, 204"           DELETE "/items?confirm=true"
Call "POST 1/2 생성 - 201 + Location"                 POST   "/items"            '{"name":"pen","price":1000}'
Call "POST 2/2 일괄 생성 - 201"                       POST   "/items/bulk"       '[{"name":"note","price":2000},{"name":"eraser","price":500}]'
Call "GET 1/2 목록 - 200"                             GET    "/items"
Call "GET 2/2 단건 - 200"                             GET    "/items/1"
Call "PUT 1/2 전체 교체 - 200"                        PUT    "/items/1"          '{"name":"pencil","price":700}'
Call "PUT 2/2 가격 교체 - 200"                        PUT    "/items/1/price"    '{"price":500}'
Call "400 검증 실패 (빈 이름, 음수 가격)"             POST   "/items"            '{"name":"","price":-1}'
Call "400 PUT에 일부 필드만 전송"                     PUT    "/items/1"          '{"name":"only-name"}'
Call "404 없는 id 조회"                               GET    "/items/999"
Call "409 이름 중복"                                  POST   "/items"            '{"name":"pencil","price":1}'
Call "DELETE 1/2 단건 삭제 - 204"                     DELETE "/items/1"
Call "404 이미 삭제된 id 삭제"                        DELETE "/items/1"
Call "400 전체 삭제 confirm 누락"                     DELETE "/items"
Call "DELETE 2/2 전체 삭제 - 204"                     DELETE "/items?confirm=true"
Call "500 서버 오류 (시연)"                           GET    "/simulate/500"
Call "503 서비스 점검 (시연)"                         GET    "/simulate/503"
