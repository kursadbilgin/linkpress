package com.kursad.linkpress.controller;

import com.kursad.linkpress.exception.GlobalExceptionHandler;
import com.kursad.linkpress.exception.ShortUrlNotFoundException;
import com.kursad.linkpress.exception.UrlInactiveException;
import com.kursad.linkpress.service.ShortUrlService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RedirectController.class)
@Import(GlobalExceptionHandler.class)
class RedirectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShortUrlService shortUrlService;

    @Test
    @DisplayName("GET /{shortCode} - should redirect with 302")
    void shouldRedirect() throws Exception {
        when(shortUrlService.getOriginalUrlForRedirect("abc123"))
                .thenReturn("https://google.com");

        mockMvc.perform(get("/abc123"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://google.com"));
    }

    @Test
    @DisplayName("GET /{shortCode} - should return 404 when not found")
    void shouldReturn404() throws Exception {
        when(shortUrlService.getOriginalUrlForRedirect("nope"))
                .thenThrow(new ShortUrlNotFoundException("nope"));

        mockMvc.perform(get("/nope"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /{shortCode} - should return 403 when inactive")
    void shouldReturn403WhenInactive() throws Exception {
        when(shortUrlService.getOriginalUrlForRedirect("disabled"))
                .thenThrow(new UrlInactiveException("disabled"));

        mockMvc.perform(get("/disabled"))
                .andExpect(status().isForbidden());
    }
}
