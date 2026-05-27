package com.urlshortener.infrastructure.ratelimit;

import com.urlshortener.exception.RateLimitExceededException;
import com.urlshortener.infrastructure.config.RateLimitConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataRedisTest
@Testcontainers
@Import({RateLimitConfig.class, RateLimitService.class})
class RateLimitServiceTest {

    @Container
    @SuppressWarnings("resource")
    static GenericContainer<?> valkey = new GenericContainer<>(DockerImageName.parse("valkey/valkey:8"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", valkey::getHost);
        registry.add("spring.data.redis.port", valkey::getFirstMappedPort);
        registry.add("shortener.rate-limit.shorten.capacity", () -> 3);
        registry.add("shortener.rate-limit.shorten.refill-tokens", () -> 3);
        registry.add("shortener.rate-limit.shorten.refill-period-seconds", () -> 60);
        registry.add("shortener.rate-limit.redirect.capacity", () -> 5);
        registry.add("shortener.rate-limit.redirect.refill-tokens", () -> 5);
        registry.add("shortener.rate-limit.redirect.refill-period-seconds", () -> 60);
    }

    @Autowired
    RateLimitService rateLimitService;

    @Test
    void shorten_withinLimit_passes() {
        assertThatCode(() -> {
            rateLimitService.checkShorten("1.1.1.1");
            rateLimitService.checkShorten("1.1.1.1");
            rateLimitService.checkShorten("1.1.1.1");
        }).doesNotThrowAnyException();
    }

    @Test
    void shorten_exceedLimit_throws() {
        for (int i = 0; i < 3; i++) {
            rateLimitService.checkShorten("2.2.2.2");
        }
        assertThatThrownBy(() -> rateLimitService.checkShorten("2.2.2.2"))
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    void redirect_withinLimit_passes() {
        assertThatCode(() -> {
            for (int i = 0; i < 5; i++) {
                rateLimitService.checkRedirect("3.3.3.3");
            }
        }).doesNotThrowAnyException();
    }

    @Test
    void redirect_exceedLimit_throws() {
        for (int i = 0; i < 5; i++) {
            rateLimitService.checkRedirect("4.4.4.4");
        }
        assertThatThrownBy(() -> rateLimitService.checkRedirect("4.4.4.4"))
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    void differentIps_independentBuckets() {
        for (int i = 0; i < 3; i++) {
            rateLimitService.checkShorten("5.5.5.5");
        }
        assertThatCode(() -> rateLimitService.checkShorten("6.6.6.6"))
                .doesNotThrowAnyException();
    }
}
