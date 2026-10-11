package com.example.ksl.wsd2025.api.v1;

import com.example.ksl.wsd2025.api.dto.ItemDto;
import com.example.ksl.wsd2025.api.request.ItemCreateRequest;
import com.example.ksl.wsd2025.api.request.ItemUpdateRequest;
import com.example.ksl.wsd2025.common.ApiException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 과제용 컨트롤러: POST/GET/PUT/DELETE 각 2개 (총 8개)
 * 응답 형식은 3주차 슬라이드를 따른다.
 *  - 성공: 객체·배열을 그대로 반환 (200 / 201 + Location / 204 본문 없음)
 *  - 오류: {"statusCode":..., "message":"..."}  (GlobalExceptionHandler)
 */
@RestController
@RequestMapping("/api/v1/items")
public class ItemController {

    // 메모리용 임시 DB (예제용)
    private final Map<Long, ItemDto> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    // ---------- POST x2 ----------

    // 1) POST 단건 생성 -> 201 + Location / 400(검증 실패) / 409(이름 중복)
    @PostMapping
    public ResponseEntity<ItemDto> createItem(@Valid @RequestBody ItemCreateRequest request) {
        ItemDto item = create(request);
        return ResponseEntity.created(location(item)).body(item);
    }

    // 2) POST 일괄 생성 -> 201 / 400(빈 목록, 잘못된 항목) / 409
    @PostMapping("/bulk")
    public ResponseEntity<List<ItemDto>> createItems(@RequestBody List<ItemCreateRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "생성할 아이템 목록이 비어 있습니다.");
        }
        for (ItemCreateRequest r : requests) {
            if (r.getName() == null || r.getName().isBlank() || r.getPrice() == null || r.getPrice() < 0) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "name은 필수이고 price는 0 이상이어야 합니다.");
            }
        }
        List<ItemDto> created = new ArrayList<>();
        for (ItemCreateRequest r : requests) {
            created.add(create(r));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ---------- GET x2 ----------

    // 3) GET 목록 조회 (선택: ?keyword=) -> 200, 없으면 []
    @GetMapping
    public List<ItemDto> getItems(@RequestParam(required = false) String keyword) {
        return store.values().stream()
                .filter(i -> keyword == null || i.getName().contains(keyword))
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .toList();
    }

    // 4) GET 단건 조회 (PathVariable 사용) -> 200 / 404
    @GetMapping("/{id}")
    public ItemDto getItem(@PathVariable Long id) {
        return findOrThrow(id);
    }

    // ---------- PUT x2 ----------

    // 5) PUT 전체 교체 (name, price 모두 필요) -> 200 / 400 / 404
    @PutMapping("/{id}")
    public ItemDto replaceItem(@PathVariable Long id, @Valid @RequestBody ItemCreateRequest request) {
        ItemDto item = findOrThrow(id);
        item.setName(request.getName());
        item.setPrice(request.getPrice());
        return item;
    }

    // 6) PUT 가격 교체 (price만 필요) -> 200 / 400(price 누락) / 404
    @PutMapping("/{id}/price")
    public ItemDto replacePrice(@PathVariable Long id, @Valid @RequestBody ItemUpdateRequest request) {
        if (request.getPrice() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "price는 필수입니다.");
        }
        ItemDto item = findOrThrow(id);
        item.setPrice(request.getPrice());
        return item;
    }

    // ---------- DELETE x2 ----------

    // 7) DELETE 단건 삭제 -> 204 No Content / 404
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        if (store.remove(id) == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "아이템을 찾을 수 없습니다. id=" + id);
        }
        return ResponseEntity.noContent().build();
    }

    // 8) DELETE 전체 삭제 (?confirm=true 필요) -> 204 No Content / 400
    @DeleteMapping
    public ResponseEntity<Void> deleteAll(@RequestParam(defaultValue = "false") boolean confirm) {
        if (!confirm) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "전체 삭제는 confirm=true 가 필요합니다.");
        }
        store.clear();
        sequence.set(1);
        return ResponseEntity.noContent().build();
    }

    // ---------- helpers ----------

    private ItemDto create(ItemCreateRequest request) {
        boolean duplicated = store.values().stream().anyMatch(i -> i.getName().equals(request.getName()));
        if (duplicated) {
            throw new ApiException(HttpStatus.CONFLICT, "이미 존재하는 이름입니다: " + request.getName());
        }
        ItemDto item = new ItemDto();
        item.setId(sequence.getAndIncrement());
        item.setName(request.getName());
        item.setPrice(request.getPrice());
        store.put(item.getId(), item);
        return item;
    }

    private ItemDto findOrThrow(Long id) {
        ItemDto item = store.get(id);
        if (item == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "아이템을 찾을 수 없습니다. id=" + id);
        }
        return item;
    }

    private URI location(ItemDto item) {
        return URI.create("/api/v1/items/" + item.getId());
    }
}
