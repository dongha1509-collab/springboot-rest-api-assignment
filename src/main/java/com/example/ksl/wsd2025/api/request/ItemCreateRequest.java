package com.example.ksl.wsd2025.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class ItemCreateRequest {
    @NotBlank(message = "name은 필수입니다.")
    private String name;

    @NotNull(message = "price는 필수입니다.")
    @PositiveOrZero(message = "price는 0 이상이어야 합니다.")
    private Integer price;

    // getter / setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
}
