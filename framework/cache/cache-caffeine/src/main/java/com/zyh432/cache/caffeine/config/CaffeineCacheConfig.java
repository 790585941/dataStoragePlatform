package com.zyh432.cache.caffeine.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.zyh432.cache.core.constants.CacheConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;

@SpringBootConfiguration
@EnableCaching
@Slf4j
public class CaffeineCacheConfig {
    @Autowired
    private CaffeineCacheProperties properties;
    //配置缓存管理
    //包括缓存的名称、是否允许空值、初始缓存容量和最大缓存容量
    @Bean
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(CacheConstants.dataStoragePlatform_CACHE_NAME);
        cacheManager.setAllowNullValues(properties.getAllowNullValue());
        Caffeine<Object, Object> caffeineBuilder = Caffeine.newBuilder()
                .initialCapacity(properties.getInitCacheCapacity())
                .maximumSize(properties.getMaxCacheCapacity());
        cacheManager.setCaffeine(caffeineBuilder);
        log.info("the caffeine cache manager is loaded successfully!");
        return cacheManager;
    }
}
