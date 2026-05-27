package com.urlshortener.infrastructure.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "shortener.rate-limit")
public record RateLimitProperties(
        @DefaultValue Limit shorten,
        @DefaultValue Limit redirect
) {
    public record Limit(
            @DefaultValue("10") long capacity,
            @DefaultValue("5") long refillTokens,
            @DefaultValue("1") long refillPeriodSeconds
    ) {}
}
