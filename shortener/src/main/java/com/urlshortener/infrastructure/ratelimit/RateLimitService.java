package com.urlshortener.infrastructure.ratelimit;

import com.urlshortener.exception.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final ProxyManager<String> proxyManager;
    private final RateLimitProperties properties;

    public void checkShorten(String ip) {
        check("rate:shorten:" + ip, properties.shorten());
    }

    public void checkRedirect(String ip) {
        check("rate:redirect:" + ip, properties.redirect());
    }

    private void check(String key, RateLimitProperties.Limit limit) {
        var config = BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(limit.capacity())
                        .refillGreedy(limit.refillTokens(), Duration.ofSeconds(limit.refillPeriodSeconds()))
                        .build())
                .build();

        var bucket = proxyManager.builder().build(key, () -> config);
        if (!bucket.tryConsume(1)) {
            throw new RateLimitExceededException(key);
        }
    }
}
