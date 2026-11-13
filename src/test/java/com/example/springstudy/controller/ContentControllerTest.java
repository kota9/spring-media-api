package com.example.springstudy.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.example.springstudy.domain.Content;
import com.example.springstudy.exception.ContentNotFoundException;
import com.example.springstudy.service.ContentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ContentController.class)
class ContentControllerTest {

    private static final String CONTENTS_URL = "/api/contents";
    private static final long CONTENT_ID = 3L;
    private static final String TITLE = "Test content";
    private static final String DESCRIPTION = "Description";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContentService contentService;

    @Test
    void returnsContentListAsJson() throws Exception {
        given(contentService.findAll()).willReturn(List.of(
                new Content(1L, TITLE, DESCRIPTION)));

        mockMvc.perform(get(CONTENTS_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value(TITLE))
                .andExpect(jsonPath("$[0].description").value(DESCRIPTION));

        verify(contentService).findAll();
    }

    @Test
    void returnsContentByPathVariable() throws Exception {
        given(contentService.findById(1L)).willReturn(new Content(1L, TITLE, DESCRIPTION));

        mockMvc.perform(get(CONTENTS_URL + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value(TITLE));

        verify(contentService).findById(1L);
    }

    @Test
    void createsContentAndReturnsLocationHeader() throws Exception {
        given(contentService.create("New content", DESCRIPTION, 9L, 12L))
                .willReturn(new Content(7L, "New content", DESCRIPTION));

        mockMvc.perform(post(CONTENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"New content","description":"Description","authorId":9,"categoryId":12}
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Location", "/api/contents/7"))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.title").value("New content"));

        verify(contentService).create("New content", DESCRIPTION, 9L, 12L);
    }

    @Test
    void updatesContent() throws Exception {
        given(contentService.update(3L, "Updated", "Updated description"))
                .willReturn(new Content(3L, "Updated", "Updated description"));

        mockMvc.perform(put(CONTENTS_URL + "/{id}", CONTENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Updated","description":"Updated description"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.title").value("Updated"));

        verify(contentService).update(3L, "Updated", "Updated description");
    }

    @Test
    void patchesContent() throws Exception {
        given(contentService.patch(eq(CONTENT_ID), any()))
                .willReturn(new Content(3L, "Patched", "Description"));

        mockMvc.perform(patch(CONTENTS_URL + "/{id}", CONTENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Patched\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Patched"));
    }

    @Test
    void deletesContentWithNoContentStatus() throws Exception {
        mockMvc.perform(delete(CONTENTS_URL + "/{id}", 5L))
                .andExpect(status().isNoContent());

        verify(contentService).delete(5L);
    }

    @Test
    void rejectsInvalidCreateRequestsWithoutCallingService() throws Exception {
        mockMvc.perform(post(CONTENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Description\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.path").value("/api/contents"));

        mockMvc.perform(post(CONTENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"   \",\"description\":\"Description\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post(CONTENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + "x".repeat(101) + "\",\"description\":\"Description\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post(CONTENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(contentService);
    }

    @Test
    void rejectsInvalidUpdateWithoutCallingService() throws Exception {
        mockMvc.perform(put(CONTENTS_URL + "/{id}", CONTENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Valid title\",\"description\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        verifyNoInteractions(contentService);
    }

    @Test
    void returnsNotFoundWhenContentDoesNotExist() throws Exception {
        willThrow(new ContentNotFoundException(999L)).given(contentService).findById(999L);
        willThrow(new ContentNotFoundException(999L)).given(contentService)
                .update(999L, "Title", "Description");
        willThrow(new ContentNotFoundException(999L)).given(contentService).delete(999L);

        mockMvc.perform(get("/api/contents/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("CONTENT_NOT_FOUND"))
                .andExpect(jsonPath("$.path").value("/api/contents/999"));

        mockMvc.perform(put("/api/contents/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Title\",\"description\":\"Description\"}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/contents/999"))
                .andExpect(status().isNotFound());
    }
}
