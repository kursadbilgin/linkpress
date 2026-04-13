package com.kursad.linkpress.service;

import com.kursad.linkpress.dto.request.CreateShortUrlRequest;
import com.kursad.linkpress.dto.response.ShortUrlResponse;
import com.kursad.linkpress.entity.ShortUrl;
import com.kursad.linkpress.mapper.ShortUrlMapper;
import com.kursad.linkpress.repository.ShortUrlRepository;
import com.kursad.linkpress.util.ShortCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShortUrlService {

    private final ShortUrlRepository shortUrlRepository;

    public ShortUrlResponse create(CreateShortUrlRequest request) {
        String shortCode = resolveShortCode(request.getCustomAlias());

        ShortUrl shortUrl = ShortUrl.builder()
                .originalUrl(request.getOriginalUrl())
                .shortCode(shortCode)
                .expiresAt(request.getExpiresAt())
                .build();

        ShortUrl saved = shortUrlRepository.save(shortUrl);
        return ShortUrlMapper.toResponse(saved);
    }

    public String getOriginalUrlForRedirect(String shortCode) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("Short URL not found: " + shortCode));

        if (!shortUrl.getActive()) {
            throw new RuntimeException("Short URL is inactive: " + shortCode);
        }

        if (shortUrl.getExpiresAt() != null && shortUrl.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("Short URL has expired: " + shortCode);
        }

        shortUrl.setClickCount(shortUrl.getClickCount() + 1);
        shortUrlRepository.save(shortUrl);

        return shortUrl.getOriginalUrl();
    }

    public ShortUrlResponse getByShortCode(String shortCode) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("Short URL not found: " + shortCode));
        return ShortUrlMapper.toResponse(shortUrl);
    }

    public List<ShortUrlResponse> getAll() {
        return shortUrlRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ShortUrlMapper::toResponse)
                .toList();
    }

    public void deactivate(String shortCode) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("Short URL not found: " + shortCode));
        shortUrl.setActive(false);
        shortUrlRepository.save(shortUrl);
    }

    public void delete(String shortCode) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("Short URL not found: " + shortCode));
        shortUrlRepository.delete(shortUrl);
    }

    private String resolveShortCode(String customAlias) {
        if (customAlias != null && !customAlias.isBlank()) {
            if (shortUrlRepository.existsByShortCode(customAlias)) {
                throw new RuntimeException("Short code already exists: " + customAlias);
            }
            return customAlias;
        }
        return generateUniqueShortCode();
    }

    private String generateUniqueShortCode() {
        String code;
        do {
            code = ShortCodeGenerator.generate();
        } while (shortUrlRepository.existsByShortCode(code));
        return code;
    }
}
