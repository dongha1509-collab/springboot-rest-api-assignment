package com.example.ksl.wsd2025.api.v1;

import com.example.ksl.wsd2025.api.dto.ItemDto;
import com.example.ksl.wsd2025.api.request.ItemCreateRequest;
import com.example.ksl.wsd2025.api.request.ItemUpdateRequest;
import com.example.ksl.wsd2025.common.ApiException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// 과제용 컨트롤러: POST / GET / PUT / DELETE 각 2개 (총 8개)
//
// 응답 형식 (3주차 슬라이드)
//  - 성공: 객체/배열 그대로 응답 (등록 201 + Location, 삭제 204 본문 없음)
//  - 실패: {"statusCode":..., "message":"..."}  -> GlobalExceptionHandler 에서 만든다
@RestController
@RequestMapping("/api/v1/items")
public class ItemController {

    // 메모리용 임시 DB (예제용)
    // LinkedHashMap: 넣은 순서대로 꺼낼 수 있다
    private final Map<Long, ItemDto> store = new LinkedHashMap<>();
    private long sequence = 1L;

    // ================= POST (2개) =================

    // 1) POST: 아이템 1개 생성 -> 201 / 400(입력 오류) / 409(이름 중복)
    @PostMapping
    public ResponseEntity<ItemDto> createItem(@RequestBody ItemCreateRequest request) {
        checkName(request.getName());
        checkPrice(request.getPrice());
        checkDuplicate(request.getName());

        ItemDto item = saveItem(request);

        return ResponseEntity.status(201)
                .header("Location", "/api/v1/items/" + item.getId())
                .body(item);
    }

    // 2) POST: 아이템 여러 개 한 번에 생성 -> 201 / 400 / 409
    @PostMapping("/bulk")
    public ResponseEntity<List<ItemDto>> createItems(@RequestBody List<ItemCreateRequest> requests) {
        if (requests.isEmpty()) {
            throw new ApiException(400, "생성할 아이템 목록이 비어 있습니다.");
        }

        // 먼저 전부 검사하고
        for (ItemCreateRequest r : requests) {
            if (r == null) {
                throw new ApiException(400, "목록에 빈 항목이 있습니다.");
            }
            checkName(r.getName());
            checkPrice(r.getPrice());
            checkDuplicate(r.getName());
        }

        // 문제가 없으면 저장
        List<ItemDto> created = new ArrayList<>();
        for (ItemCreateRequest r : requests) {
            created.add(saveItem(r));
        }

        return ResponseEntity.status(201).body(created);
    }

    // ================= GET (2개) =================

    // 3) GET: 전체 조회 (keyword가 있으면 이름에 포함된 것만) -> 200
    @GetMapping
    public ResponseEntity<List<ItemDto>> getItems(@RequestParam(required = false) String keyword) {
        List<ItemDto> result = new ArrayList<>();

        for (ItemDto item : store.values()) {
            if (keyword == null || item.getName().contains(keyword)) {
                result.add(item);
            }
        }

        return ResponseEntity.ok(result);
    }

    // 4) GET: 단건 조회 (PathVariable 사용) -> 200 / 404
    @GetMapping("/{id}")
    public ResponseEntity<ItemDto> getItem(@PathVariable Long id) {
        ItemDto item = findItem(id);
        return ResponseEntity.ok(item);
    }

    // ================= PUT (2개) =================

    // 5) PUT: 아이템 전체 교체 (name, price 둘 다 필요) -> 200 / 400 / 404
    @PutMapping("/{id}")
    public ResponseEntity<ItemDto> replaceItem(
            @PathVariable Long id,
            @RequestBody ItemCreateRequest request
    ) {
        ItemDto item = findItem(id);

        checkName(request.getName());
        checkPrice(request.getPrice());

        item.setName(request.getName());
        item.setPrice(request.getPrice());

        return ResponseEntity.ok(item);
    }

    // 6) PUT: 가격만 교체 (price만 필요) -> 200 / 400 / 404
    @PutMapping("/{id}/price")
    public ResponseEntity<ItemDto> replacePrice(
            @PathVariable Long id,
            @RequestBody ItemUpdateRequest request
    ) {
        ItemDto item = findItem(id);

        checkPrice(request.getPrice());

        item.setPrice(request.getPrice());

        return ResponseEntity.ok(item);
    }

    // ================= DELETE (2개) =================

    // 7) DELETE: 아이템 1개 삭제 -> 204 (본문 없음) / 404
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        findItem(id); // 없으면 여기서 404
        store.remove(id);
        return ResponseEntity.noContent().build();
    }

    // 8) DELETE: 전체 삭제 (실수 방지를 위해 ?confirm=true 필요) -> 204 / 400
    @DeleteMapping
    public ResponseEntity<Void> deleteAll(@RequestParam(defaultValue = "false") boolean confirm) {
        if (!confirm) {
            throw new ApiException(400, "전체 삭제는 confirm=true 가 필요합니다.");
        }

        store.clear();
        sequence = 1L;
        return ResponseEntity.noContent().build();
    }

    // ================= 도우미 메소드 =================

    // id로 아이템 찾기 (없으면 404)
    private ItemDto findItem(Long id) {
        ItemDto item = store.get(id);
        if (item == null) {
            throw new ApiException(404, "아이템을 찾을 수 없습니다. id=" + id);
        }
        return item;
    }

    // name 검사 (비어 있으면 400)
    private void checkName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ApiException(400, "name은 필수입니다.");
        }
    }

    // price 검사 (없거나 음수면 400)
    private void checkPrice(Integer price) {
        if (price == null) {
            throw new ApiException(400, "price는 필수입니다.");
        }
        if (price < 0) {
            throw new ApiException(400, "price는 0 이상이어야 합니다.");
        }
    }

    // 같은 이름이 이미 있으면 409
    private void checkDuplicate(String name) {
        for (ItemDto item : store.values()) {
            if (item.getName().equals(name)) {
                throw new ApiException(409, "이미 존재하는 이름입니다: " + name);
            }
        }
    }

    // 아이템 저장하고 만들어진 아이템 돌려주기
    private ItemDto saveItem(ItemCreateRequest request) {
        ItemDto item = new ItemDto();
        item.setId(sequence);
        sequence++;
        item.setName(request.getName());
        item.setPrice(request.getPrice());

        store.put(item.getId(), item);
        return item;
    }
}
