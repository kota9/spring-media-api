package com.example.springstudy.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import com.example.springstudy.service.ContentService;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;


@SpringBootTest
@AutoConfigureMockMvc
class ContentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoSpyBean
    private ContentService contentService;

    @Test
    void createReadPutPatchAndDeleteContent() throws Exception {
        MvcResult createdResult = mockMvc.perform(post("/api/contents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"REST API 학습","description":"첫 설명"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.title").value("REST API 학습"))
                .andReturn();
        long id = readId(createdResult);

        mockMvc.perform(get("/api/contents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + id + ")].title").value("REST API 학습"));
        mockMvc.perform(get("/api/contents/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("첫 설명"));

        mockMvc.perform(put("/api/contents/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"전체 수정","description":"PUT 설명"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("전체 수정"))
                .andExpect(jsonPath("$.description").value("PUT 설명"));

        mockMvc.perform(patch("/api/contents/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"부분 수정"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("부분 수정"))
                .andExpect(jsonPath("$.description").value("PUT 설명"));

        mockMvc.perform(delete("/api/contents/{id}", id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/contents/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("404"));
    }

    @Test
    void rejectsInvalidCreateAndPatchRequests() throws Exception {
        clearInvocations(contentService);
        mockMvc.perform(post("/api/contents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"   ","description":"설명"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("요청 값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.errors.title[0]").value("제목은 필수입니다."));
        verify(contentService, never()).create(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());

        mockMvc.perform(post("/api/contents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + "x".repeat(101) + "\",\"description\":\"설명\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title[0]").value("제목은 100자 이하여야 합니다."));

        MvcResult result = mockMvc.perform(post("/api/contents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"PATCH 검증","description":"기존 설명"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        long id = readId(result);

        mockMvc.perform(patch("/api/contents/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(patch("/api/contents/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":null}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/contents/999999999"))
                .andExpect(status().isNotFound());
    }

    private long readId(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        return Long.parseLong(body.replaceAll(".*\\\"id\\\":(\\d+).*", "$1"));
    }
}
