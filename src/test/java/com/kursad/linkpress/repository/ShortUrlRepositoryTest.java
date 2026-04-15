package com.kursad.linkpress.repository;

import com.kursad.linkpress.entity.ShortUrl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ShortUrlRepositoryTest {

    @Autowired
    private ShortUrlRepository shortUrlRepository;

    @BeforeEach
    void setUp() {
        shortUrlRepository.deleteAll();
    }

    @Test
    @DisplayName("should find by short code")
    void shouldFindByShortCode() {
        ShortUrl entity = ShortUrl.builder()
                .originalUrl("https://google.com")
                .shortCode("test123")
                .build();
        shortUrlRepository.save(entity);

        Optional<ShortUrl> found = shortUrlRepository.findByShortCode("test123");

        assertThat(found).isPresent();
        assertThat(found.get().getOriginalUrl()).isEqualTo("https://google.com");
    }

    @Test
    @DisplayName("should return empty when short code not found")
    void shouldReturnEmptyWhenNotFound() {
        Optional<ShortUrl> found = shortUrlRepository.findByShortCode("nonexistent");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("should check if short code exists")
    void shouldCheckExistence() {
        ShortUrl entity = ShortUrl.builder()
                .originalUrl("https://google.com")
                .shortCode("exists1")
                .build();
        shortUrlRepository.save(entity);

        assertThat(shortUrlRepository.existsByShortCode("exists1")).isTrue();
        assertThat(shortUrlRepository.existsByShortCode("nope")).isFalse();
    }

    @Test
    @DisplayName("should return all ordered by created date desc")
    void shouldReturnAllOrdered() {
        ShortUrl first = ShortUrl.builder()
                .originalUrl("https://first.com")
                .shortCode("first11")
                .build();
        shortUrlRepository.save(first);

        ShortUrl second = ShortUrl.builder()
                .originalUrl("https://second.com")
                .shortCode("second1")
                .build();
        shortUrlRepository.save(second);

        List<ShortUrl> all = shortUrlRepository.findAllByOrderByCreatedAtDesc();

        assertThat(all).hasSize(2);
        assertThat(all.get(0).getShortCode()).isEqualTo("second1");
        assertThat(all.get(1).getShortCode()).isEqualTo("first11");
    }
}
