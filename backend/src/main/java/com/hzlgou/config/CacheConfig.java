package com.hzlgou.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 缓存配置类
 */
@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                // 设置缓存过期时间为30天
                .expireAfterWrite(30, TimeUnit.DAYS)
                // 初始缓存大小
                .initialCapacity(1000)
                // 最大缓存大小
                .maximumSize(10000)
                // 统计缓存命中率
                .recordStats());
        return cacheManager;
    }
}
