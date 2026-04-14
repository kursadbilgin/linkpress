package com.kursad.linkpress.controller;

import com.kursad.linkpress.dto.request.CreateShortUrlRequest;
import com.kursad.linkpress.dto.response.ShortUrlResponse;
import com.kursad.linkpress.service.ShortUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/urls")
@RequiredArgsConstructor
public class UrlManagementController {

    private final ShortUrlService shortUrlService;

    @PostMapping
    public ResponseEntity<ShortUrlResponse> create(@RequestBody CreateShortUrlRequest request) {
        ShortUrlResponse response = shortUrlService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ShortUrlResponse>> getAll() {
        return ResponseEntity.ok(shortUrlService.getAll());
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<ShortUrlResponse> getByShortCode(@PathVariable String shortCode) {
        return ResponseEntity.ok(shortUrlService.getByShortCode(shortCode));
    }

    @PatchMapping("/{shortCode}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable String shortCode) {
        shortUrlService.deactivate(shortCode);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{shortCode}")
    public ResponseEntity<Void> delete(@PathVariable String shortCode) {
        shortUrlService.delete(shortCode);
        return ResponseEntity.noContent().build();
    }
}
