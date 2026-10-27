package com.example.springstudy.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class CommentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void supportsCommentCrudForAContent() throws Exception {
        long contentId = createContent();
        long userId = createUser();
        MvcResult created = createComment(contentId, userId, "First comment")
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.contentId").value(contentId))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.body").value("First comment"))
                .andReturn();
        long commentId = readId(created);

        mockMvc.perform(get("/api/contents/{contentId}/comments", contentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(commentId))
                .andExpect(jsonPath("$[0].body").value("First comment"));
        mockMvc.perform(get("/api/comments/{commentId}", commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentId));

        mockMvc.perform(put("/api/comments/{commentId}", commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"Updated comment\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body").value("Updated comment"));
        mockMvc.perform(get("/api/comments/{commentId}", commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body").value("Updated comment"));

        mockMvc.perform(delete("/api/comments/{commentId}", commentId))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/comments/{commentId}", commentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("COMMENT_NOT_FOUND"));
    }

    @Test
    void validatesContentUserAndCommentRequests() throws Exception {
        long contentId = createContent();
        long userId = createUser();

        createComment(999999999L, userId, "Invalid content")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CONTENT_NOT_FOUND"));
        createComment(contentId, 999999999L, "Invalid user")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));

        mockMvc.perform(post("/api/contents/{contentId}/comments", contentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":" + userId + ",\"body\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        mockMvc.perform(post("/api/contents/{contentId}/comments", contentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":" + userId + ",\"body\":\"" + "x".repeat(1001) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        mockMvc.perform(put("/api/comments/999999999").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        mockMvc.perform(get("/api/contents/999999999/comments"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CONTENT_NOT_FOUND"));
        mockMvc.perform(get("/api/comments/999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("COMMENT_NOT_FOUND"));
        mockMvc.perform(put("/api/comments/999999999").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"Updated\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("COMMENT_NOT_FOUND"));
        mockMvc.perform(delete("/api/comments/999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("COMMENT_NOT_FOUND"));
    }

    @Test
    void deletingContentOrUserAlsoDeletesTheirComments() throws Exception {
        long userId = createUser();
        long contentId = createContent();
        long contentCommentId = readId(createComment(contentId, userId, "Removed with content")
                .andExpect(status().isCreated()).andReturn());

        mockMvc.perform(delete("/api/contents/{contentId}", contentId))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/comments/{commentId}", contentCommentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("COMMENT_NOT_FOUND"));

        long anotherContentId = createContent();
        long userCommentId = readId(createComment(anotherContentId, userId, "Removed with user")
                .andExpect(status().isCreated()).andReturn());
        mockMvc.perform(delete("/api/users/{userId}", userId))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/comments/{commentId}", userCommentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("COMMENT_NOT_FOUND"));
    }

    private long createContent() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/contents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + UUID.randomUUID() + "\",\"description\":\"Description\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return readId(result);
    }

    private long createUser() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Comment User\",\"email\":\""
                                + UUID.randomUUID() + "@example.com\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return readId(result);
    }

    private org.springframework.test.web.servlet.ResultActions createComment(
            long contentId, long userId, String body) throws Exception {
        return mockMvc.perform(post("/api/contents/{contentId}/comments", contentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":" + userId + ",\"body\":\"" + body + "\"}"));
    }

    private long readId(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        return Long.parseLong(body.replaceAll(".*\\\"id\\\":(\\d+).*", "$1"));
    }
}
