package com.kursad.linkpress.mapper;

import com.kursad.linkpress.dto.response.ShortUrlResponse;
import com.kursad.linkpress.entity.ShortUrl;

public class ShortUrlMapper {

    private static final String BASE_URL = "http://localhost:8080/";

    private ShortUrlMapper() {
    }

    public static ShortUrlResponse toResponse(ShortUrl entity) {
        return ShortUrlResponse.builder()
                .id(entity.getId())
                .originalUrl(entity.getOriginalUrl())
                .shortCode(entity.getShortCode())
                .shortUrl(BASE_URL + entity.getShortCode())
                .active(entity.getActive())
                .clickCount(entity.getClickCount())
                .expiresAt(entity.getExpiresAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
