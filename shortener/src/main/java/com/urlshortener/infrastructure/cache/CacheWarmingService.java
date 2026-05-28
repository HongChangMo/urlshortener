package com.urlshortener.infrastructure.cache;

import com.urlshortener.domain.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheWarmingService {

    private final UrlRepository urlRepository;
    private final UrlCacheService urlCacheService;

    @Value("${shortener.cache-warm-size:1000}")
    private int cacheWarmSize;

    @EventListener(ApplicationReadyEvent.class)
    public void warmCache() {
        log.info("Cache warming 시작 — top {} URL 조회", cacheWarmSize);

        var urls = urlRepository.findTopNByAccessCount(cacheWarmSize);
        urls.forEach(url -> urlCacheService.put(url.getShortCode(), url.getOriginalUrl()));

        log.info("Cache warming 완료 — {}개 URL 선적재", urls.size());
    }
}
