package com.urlshortener.infrastructure.cache;

import com.urlshortener.domain.Url;
import com.urlshortener.domain.repository.UrlRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheWarmingServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private UrlCacheService urlCacheService;

    @InjectMocks
    private CacheWarmingService cacheWarmingService;

    @Test
    void warmCache_적재할_URL이_있으면_캐시에_선적재한다() {
        ReflectionTestUtils.setField(cacheWarmingService, "cacheWarmSize", 3);

        Url url1 = mock(Url.class);
        Url url2 = mock(Url.class);
        when(url1.getShortCode()).thenReturn("abc123");
        when(url1.getOriginalUrl()).thenReturn("https://example.com/1");
        when(url2.getShortCode()).thenReturn("def456");
        when(url2.getOriginalUrl()).thenReturn("https://example.com/2");
        when(urlRepository.findTopNByAccessCount(3)).thenReturn(List.of(url1, url2));

        cacheWarmingService.warmCache();

        verify(urlCacheService).put("abc123", "https://example.com/1");
        verify(urlCacheService).put("def456", "https://example.com/2");
    }

    @Test
    void warmCache_URL이_없으면_캐시에_아무것도_적재하지_않는다() {
        ReflectionTestUtils.setField(cacheWarmingService, "cacheWarmSize", 1000);
        when(urlRepository.findTopNByAccessCount(1000)).thenReturn(List.of());

        cacheWarmingService.warmCache();

        verifyNoInteractions(urlCacheService);
    }

    @Test
    void warmCache_설정된_size만큼_조회한다() {
        ReflectionTestUtils.setField(cacheWarmingService, "cacheWarmSize", 500);
        when(urlRepository.findTopNByAccessCount(500)).thenReturn(List.of());

        cacheWarmingService.warmCache();

        verify(urlRepository).findTopNByAccessCount(500);
    }
}
