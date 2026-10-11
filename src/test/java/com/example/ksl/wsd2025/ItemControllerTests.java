package com.example.ksl.wsd2025;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ItemControllerTests {

    @Autowired
    MockMvc mvc;

    private static final String BASE = "/api/v1/items";

    @BeforeEach
    void reset() throws Exception {
        mvc.perform(delete(BASE).param("confirm", "true")).andExpect(status().isNoContent());
    }

    private void create(String name, int price) throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"" + name + "\",\"price\":" + price + "}"))
                .andExpect(status().isCreated());
    }

    @Test
    void post_201_with_location_and_plain_body() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"pen\",\"price\":1000}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/items/1"))
                .andExpect(header().exists("X-Request-Id"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("pen"))
                .andExpect(jsonPath("$.status").doesNotExist());
    }

    @Test
    void post_400_validation_uses_statusCode_message() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\",\"price\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void post_409_duplicate() throws Exception {
        create("pen", 1000);
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"pen\",\"price\":1}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(409));
    }

    @Test
    void post_bulk_201_and_400() throws Exception {
        mvc.perform(post(BASE + "/bulk").contentType(MediaType.APPLICATION_JSON)
                .content("[{\"name\":\"a\",\"price\":1},{\"name\":\"b\",\"price\":2}]"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(2));
        mvc.perform(post(BASE + "/bulk").contentType(MediaType.APPLICATION_JSON).content("[]"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void get_list_returns_array_and_empty_array() throws Exception {
        mvc.perform(get(BASE)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        create("pen", 1000);
        mvc.perform(get(BASE)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get(BASE).param("keyword", "zzz")).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void get_one_200_404_400() throws Exception {
        create("pen", 1000);
        mvc.perform(get(BASE + "/1")).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("pen"));
        mvc.perform(get(BASE + "/999")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404));
        mvc.perform(get(BASE + "/abc")).andExpect(status().isBadRequest());
    }

    @Test
    void put_replace_requires_whole_representation() throws Exception {
        create("pen", 1000);
        mvc.perform(put(BASE + "/1").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"pencil\",\"price\":700}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("pencil"))
                .andExpect(jsonPath("$.price").value(700));
        // 일부 필드만 보내면 전체 교체가 아니므로 400
        mvc.perform(put(BASE + "/1").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"x\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put(BASE + "/999").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"x\",\"price\":1}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void put_price() throws Exception {
        create("pen", 1000);
        mvc.perform(put(BASE + "/1/price").contentType(MediaType.APPLICATION_JSON).content("{\"price\":500}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.price").value(500));
        mvc.perform(put(BASE + "/1/price").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put(BASE + "/999/price").contentType(MediaType.APPLICATION_JSON).content("{\"price\":1}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_204_no_body_and_404() throws Exception {
        create("pen", 1000);
        mvc.perform(delete(BASE + "/1")).andExpect(status().isNoContent()).andExpect(content().string(""));
        mvc.perform(delete(BASE + "/1")).andExpect(status().isNotFound());
        mvc.perform(delete(BASE)).andExpect(status().isBadRequest());
        mvc.perform(delete(BASE).param("confirm", "true")).andExpect(status().isNoContent());
    }

    @Test
    void server_errors_500_hides_detail_and_503() throws Exception {
        mvc.perform(get("/api/v1/simulate/500")).andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.statusCode").value(500))
                .andExpect(jsonPath("$.message").value("서버 처리 중 오류가 발생했습니다."));
        mvc.perform(get("/api/v1/simulate/503")).andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.statusCode").value(503));
    }
}
