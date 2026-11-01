package com.example.springstudy.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.springstudy.service.ContentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class ContentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoSpyBean
    private ContentService contentService;

    @BeforeEach
    void clearServiceInvocations() {
        clearInvocations(contentService);
    }

    @Test
    void createsAndFindsContent() throws Exception {
        long id = createContent("REST API", "First description");

        mockMvc.perform(get("/api/contents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + id + ")].title").value("REST API"));

        mockMvc.perform(get("/api/contents/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.description").value("First description"));
    }

    @Test
    void updatesAndPatchesContent() throws Exception {
        long id = createContent("Original title", "Original description");

        mockMvc.perform(put("/api/contents/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Updated title","description":"Updated description"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.description").value("Updated description"));

        mockMvc.perform(patch("/api/contents/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Patched title"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Patched title"))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void deletesContent() throws Exception {
        long id = createContent("Content to delete", "Description");

        mockMvc.perform(delete("/api/contents/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/contents/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CONTENT_NOT_FOUND"));
    }

    @Test
    void rejectsInvalidCreateRequests() throws Exception {
        mockMvc.perform(post("/api/contents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"   ","description":"Description"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        verify(contentService, never()).create(any(), any());

        mockMvc.perform(post("/api/contents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Valid title"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        mockMvc.perform(post("/api/contents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Valid title","description":"   "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        mockMvc.perform(post("/api/contents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + "x".repeat(101) + "\",\"description\":\"Description\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        mockMvc.perform(post("/api/contents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void rejectsInvalidPatchRequests() throws Exception {
        long id = createContent("Valid title", "Valid description");

        mockMvc.perform(patch("/api/contents/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_CONTENT_PATCH"));

        mockMvc.perform(patch("/api/contents/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":null}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_CONTENT_PATCH"));

        mockMvc.perform(patch("/api/contents/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"   "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_CONTENT_PATCH"));

        mockMvc.perform(patch("/api/contents/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + "x".repeat(101) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_CONTENT_PATCH"));
    }

    @Test
    void returnsNotFoundForMissingContent() throws Exception {
        mockMvc.perform(get("/api/contents/999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CONTENT_NOT_FOUND"));

        mockMvc.perform(put("/api/contents/999999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Valid title","description":"Valid description"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CONTENT_NOT_FOUND"));

        mockMvc.perform(delete("/api/contents/999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CONTENT_NOT_FOUND"));
    }

    @Test
    void hidesDetailsFromUnexpectedErrors() throws Exception {
        doThrow(new IllegalStateException("secret SQL and implementation detail"))
                .when(contentService).findById(777L);

        mockMvc.perform(get("/api/contents/777"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.path").value("/api/contents/777"))
                .andExpect(jsonPath("$.message").value("서버 오류가 발생했습니다."))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("secret"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("IllegalStateException"))));
    }

    private long createContent(String title, String description) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/contents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + title + "\",\"description\":\"" + description + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn();
        return readId(result);
    }

    private long readId(MvcResult result) throws Exception {
        String location = result.getResponse().getHeader("Location");
        return Long.parseLong(location.substring(location.lastIndexOf('/') + 1));
    }
}
