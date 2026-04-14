package com.kursad.linkpress.service;

import com.kursad.linkpress.dto.request.CreateShortUrlRequest;
import com.kursad.linkpress.dto.response.ShortUrlResponse;
import com.kursad.linkpress.entity.ShortUrl;
import com.kursad.linkpress.exception.ShortCodeAlreadyExistsException;
import com.kursad.linkpress.exception.ShortUrlNotFoundException;
import com.kursad.linkpress.exception.UrlExpiredException;
import com.kursad.linkpress.exception.UrlInactiveException;
import com.kursad.linkpress.mapper.ShortUrlMapper;
import com.kursad.linkpress.repository.ShortUrlRepository;
import com.kursad.linkpress.util.ShortCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
        ShortUrl shortUrl = findByShortCodeOrThrow(shortCode);

        if (!shortUrl.getActive()) {
            throw new UrlInactiveException(shortCode);
        }

        if (shortUrl.getExpiresAt() != null && shortUrl.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UrlExpiredException(shortCode);
        }

        shortUrl.setClickCount(shortUrl.getClickCount() + 1);
        shortUrlRepository.save(shortUrl);

        return shortUrl.getOriginalUrl();
    }

    public ShortUrlResponse getByShortCode(String shortCode) {
        ShortUrl shortUrl = findByShortCodeOrThrow(shortCode);
        return ShortUrlMapper.toResponse(shortUrl);
    }

    public List<ShortUrlResponse> getAll() {
        return shortUrlRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ShortUrlMapper::toResponse)
                .toList();
    }

    public void deactivate(String shortCode) {
        ShortUrl shortUrl = findByShortCodeOrThrow(shortCode);
        shortUrl.setActive(false);
        shortUrlRepository.save(shortUrl);
    }

    public void delete(String shortCode) {
        ShortUrl shortUrl = findByShortCodeOrThrow(shortCode);
        shortUrlRepository.delete(shortUrl);
    }

    private ShortUrl findByShortCodeOrThrow(String shortCode) {
        return shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortUrlNotFoundException(shortCode));
    }

    private String resolveShortCode(String customAlias) {
        if (customAlias != null && !customAlias.isBlank()) {
            if (shortUrlRepository.existsByShortCode(customAlias)) {
                throw new ShortCodeAlreadyExistsException(customAlias);
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
