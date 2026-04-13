package com.kursad.linkpress.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateShortUrlRequest {

    private String originalUrl;

    private String customAlias;

    private LocalDateTime expiresAt;
}
