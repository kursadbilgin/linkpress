package com.kursad.linkpress.service;

import com.kursad.linkpress.dto.request.CreateShortUrlRequest;
import com.kursad.linkpress.dto.response.ShortUrlResponse;
import com.kursad.linkpress.entity.ShortUrl;
import com.kursad.linkpress.exception.ShortCodeAlreadyExistsException;
import com.kursad.linkpress.exception.ShortUrlNotFoundException;
import com.kursad.linkpress.exception.UrlExpiredException;
import com.kursad.linkpress.exception.UrlInactiveException;
import com.kursad.linkpress.repository.ShortUrlRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShortUrlServiceTest {

    @Mock
    private ShortUrlRepository shortUrlRepository;

    @InjectMocks
    private ShortUrlService shortUrlService;

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should create short url with auto-generated code")
        void shouldCreateWithAutoCode() {
            CreateShortUrlRequest request = new CreateShortUrlRequest("https://google.com", null, null);

            when(shortUrlRepository.existsByShortCode(anyString())).thenReturn(false);
            when(shortUrlRepository.save(any(ShortUrl.class))).thenAnswer(invocation -> {
                ShortUrl entity = invocation.getArgument(0);
                entity.setId(1L);
                entity.setCreatedAt(LocalDateTime.now());
                return entity;
            });

            ShortUrlResponse response = shortUrlService.create(request);

            assertThat(response.getOriginalUrl()).isEqualTo("https://google.com");
            assertThat(response.getShortCode()).isNotBlank();
            assertThat(response.getShortCode()).hasSize(7);
            verify(shortUrlRepository).save(any(ShortUrl.class));
        }

        @Test
        @DisplayName("should create short url with custom alias")
        void shouldCreateWithCustomAlias() {
            CreateShortUrlRequest request = new CreateShortUrlRequest("https://google.com", "my-link", null);

            when(shortUrlRepository.existsByShortCode("my-link")).thenReturn(false);
            when(shortUrlRepository.save(any(ShortUrl.class))).thenAnswer(invocation -> {
                ShortUrl entity = invocation.getArgument(0);
                entity.setId(1L);
                entity.setCreatedAt(LocalDateTime.now());
                return entity;
            });

            ShortUrlResponse response = shortUrlService.create(request);

            assertThat(response.getShortCode()).isEqualTo("my-link");
        }

        @Test
        @DisplayName("should throw exception when custom alias already exists")
        void shouldThrowWhenAliasExists() {
            CreateShortUrlRequest request = new CreateShortUrlRequest("https://google.com", "taken", null);

            when(shortUrlRepository.existsByShortCode("taken")).thenReturn(true);

            assertThatThrownBy(() -> shortUrlService.create(request))
                    .isInstanceOf(ShortCodeAlreadyExistsException.class)
                    .hasMessageContaining("taken");
        }
    }

    @Nested
    @DisplayName("getOriginalUrlForRedirect()")
    class Redirect {

        @Test
        @DisplayName("should return original url and increment click count")
        void shouldRedirectAndIncrementClick() {
            ShortUrl entity = ShortUrl.builder()
                    .id(1L)
                    .originalUrl("https://google.com")
                    .shortCode("abc123")
                    .active(true)
                    .clickCount(5L)
                    .build();

            when(shortUrlRepository.findByShortCode("abc123")).thenReturn(Optional.of(entity));
            when(shortUrlRepository.save(any(ShortUrl.class))).thenReturn(entity);

            String result = shortUrlService.getOriginalUrlForRedirect("abc123");

            assertThat(result).isEqualTo("https://google.com");
            assertThat(entity.getClickCount()).isEqualTo(6L);
            verify(shortUrlRepository).save(entity);
        }

        @Test
        @DisplayName("should throw when short code not found")
        void shouldThrowWhenNotFound() {
            when(shortUrlRepository.findByShortCode("nope")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> shortUrlService.getOriginalUrlForRedirect("nope"))
                    .isInstanceOf(ShortUrlNotFoundException.class);
        }

        @Test
        @DisplayName("should throw when url is inactive")
        void shouldThrowWhenInactive() {
            ShortUrl entity = ShortUrl.builder()
                    .id(1L)
                    .originalUrl("https://google.com")
                    .shortCode("abc123")
                    .active(false)
                    .clickCount(0L)
                    .build();

            when(shortUrlRepository.findByShortCode("abc123")).thenReturn(Optional.of(entity));

            assertThatThrownBy(() -> shortUrlService.getOriginalUrlForRedirect("abc123"))
                    .isInstanceOf(UrlInactiveException.class);
        }

        @Test
        @DisplayName("should throw when url has expired")
        void shouldThrowWhenExpired() {
            ShortUrl entity = ShortUrl.builder()
                    .id(1L)
                    .originalUrl("https://google.com")
                    .shortCode("abc123")
                    .active(true)
                    .clickCount(0L)
                    .expiresAt(LocalDateTime.now().minusDays(1))
                    .build();

            when(shortUrlRepository.findByShortCode("abc123")).thenReturn(Optional.of(entity));

            assertThatThrownBy(() -> shortUrlService.getOriginalUrlForRedirect("abc123"))
                    .isInstanceOf(UrlExpiredException.class);
        }
    }

    @Nested
    @DisplayName("getByShortCode()")
    class GetByShortCode {

        @Test
        @DisplayName("should return short url response")
        void shouldReturnResponse() {
            ShortUrl entity = ShortUrl.builder()
                    .id(1L)
                    .originalUrl("https://google.com")
                    .shortCode("abc123")
                    .active(true)
                    .clickCount(3L)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(shortUrlRepository.findByShortCode("abc123")).thenReturn(Optional.of(entity));

            ShortUrlResponse response = shortUrlService.getByShortCode("abc123");

            assertThat(response.getShortCode()).isEqualTo("abc123");
            assertThat(response.getClickCount()).isEqualTo(3L);
        }
    }

    @Nested
    @DisplayName("getAll()")
    class GetAll {

        @Test
        @DisplayName("should return all urls ordered by created date")
        void shouldReturnAll() {
            ShortUrl entity1 = ShortUrl.builder()
                    .id(1L).originalUrl("https://a.com").shortCode("aaa")
                    .active(true).clickCount(0L).createdAt(LocalDateTime.now())
                    .build();
            ShortUrl entity2 = ShortUrl.builder()
                    .id(2L).originalUrl("https://b.com").shortCode("bbb")
                    .active(true).clickCount(0L).createdAt(LocalDateTime.now())
                    .build();

            when(shortUrlRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(entity1, entity2));

            List<ShortUrlResponse> result = shortUrlService.getAll();

            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("deactivate()")
    class Deactivate {

        @Test
        @DisplayName("should set active to false")
        void shouldDeactivate() {
            ShortUrl entity = ShortUrl.builder()
                    .id(1L).originalUrl("https://google.com").shortCode("abc123")
                    .active(true).clickCount(0L)
                    .build();

            when(shortUrlRepository.findByShortCode("abc123")).thenReturn(Optional.of(entity));
            when(shortUrlRepository.save(any(ShortUrl.class))).thenReturn(entity);

            shortUrlService.deactivate("abc123");

            assertThat(entity.getActive()).isFalse();
            verify(shortUrlRepository).save(entity);
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("should delete short url")
        void shouldDelete() {
            ShortUrl entity = ShortUrl.builder()
                    .id(1L).originalUrl("https://google.com").shortCode("abc123")
                    .active(true).clickCount(0L)
                    .build();

            when(shortUrlRepository.findByShortCode("abc123")).thenReturn(Optional.of(entity));

            shortUrlService.delete("abc123");

            verify(shortUrlRepository).delete(entity);
        }
    }
}
