package com.example.ksl.wsd2025.api.request;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class ItemUpdateRequest {
    @Size(min = 1, message = "name은 비어 있을 수 없습니다.")
    private String name;

    @PositiveOrZero(message = "price는 0 이상이어야 합니다.")
    private Integer price;

    // getter / setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
}
