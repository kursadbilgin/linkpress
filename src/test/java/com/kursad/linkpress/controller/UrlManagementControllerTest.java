package com.kursad.linkpress.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kursad.linkpress.dto.request.CreateShortUrlRequest;
import com.kursad.linkpress.dto.response.ShortUrlResponse;
import com.kursad.linkpress.exception.GlobalExceptionHandler;
import com.kursad.linkpress.exception.ShortCodeAlreadyExistsException;
import com.kursad.linkpress.exception.ShortUrlNotFoundException;
import com.kursad.linkpress.service.ShortUrlService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UrlManagementController.class)
@Import(GlobalExceptionHandler.class)
class UrlManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ShortUrlService shortUrlService;

    @Test
    @DisplayName("POST /api/v1/urls - should create short url and return 201")
    void shouldCreateShortUrl() throws Exception {
        ShortUrlResponse response = ShortUrlResponse.builder()
                .id(1L)
                .originalUrl("https://google.com")
                .shortCode("abc1234")
                .shortUrl("http://localhost:8080/abc1234")
                .active(true)
                .clickCount(0L)
                .createdAt(LocalDateTime.now())
                .build();

        when(shortUrlService.create(any(CreateShortUrlRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"originalUrl\":\"https://google.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").value("abc1234"))
                .andExpect(jsonPath("$.originalUrl").value("https://google.com"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/urls - should return 400 when originalUrl is blank")
    void shouldReturn400WhenUrlBlank() throws Exception {
        mockMvc.perform(post("/api/v1/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"originalUrl\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/urls - should return 400 when originalUrl is missing")
    void shouldReturn400WhenUrlMissing() throws Exception {
        mockMvc.perform(post("/api/v1/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/v1/urls - should return 400 when url format is invalid")
    void shouldReturn400WhenUrlInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"originalUrl\":\"not-a-url\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/urls - should return 409 when alias already exists")
    void shouldReturn409WhenAliasExists() throws Exception {
        when(shortUrlService.create(any(CreateShortUrlRequest.class)))
                .thenThrow(new ShortCodeAlreadyExistsException("taken"));

        mockMvc.perform(post("/api/v1/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"originalUrl\":\"https://google.com\",\"customAlias\":\"taken\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("GET /api/v1/urls - should return all urls")
    void shouldReturnAllUrls() throws Exception {
        ShortUrlResponse response = ShortUrlResponse.builder()
                .id(1L).originalUrl("https://google.com").shortCode("abc1234")
                .shortUrl("http://localhost:8080/abc1234").active(true).clickCount(0L)
                .createdAt(LocalDateTime.now()).build();

        when(shortUrlService.getAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/urls"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].shortCode").value("abc1234"));
    }

    @Test
    @DisplayName("GET /api/v1/urls/{shortCode} - should return url details")
    void shouldReturnUrlDetails() throws Exception {
        ShortUrlResponse response = ShortUrlResponse.builder()
                .id(1L).originalUrl("https://google.com").shortCode("abc1234")
                .shortUrl("http://localhost:8080/abc1234").active(true).clickCount(5L)
                .createdAt(LocalDateTime.now()).build();

        when(shortUrlService.getByShortCode("abc1234")).thenReturn(response);

        mockMvc.perform(get("/api/v1/urls/abc1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clickCount").value(5));
    }

    @Test
    @DisplayName("GET /api/v1/urls/{shortCode} - should return 404 when not found")
    void shouldReturn404WhenNotFound() throws Exception {
        when(shortUrlService.getByShortCode("nope"))
                .thenThrow(new ShortUrlNotFoundException("nope"));

        mockMvc.perform(get("/api/v1/urls/nope"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PATCH /api/v1/urls/{shortCode}/deactivate - should return 204")
    void shouldDeactivate() throws Exception {
        mockMvc.perform(patch("/api/v1/urls/abc1234/deactivate"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/urls/{shortCode} - should return 204")
    void shouldDelete() throws Exception {
        mockMvc.perform(delete("/api/v1/urls/abc1234"))
                .andExpect(status().isNoContent());
    }
}
