package com.zyh432.cache.caffeine.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Caffeine Cache自定义配置属性类
 */
@Data
@Component
@ConfigurationProperties(prefix = "com.zyh432.cache.caffeine")
public class CaffeineCacheProperties {
    /**
     * 缓存初始容量
     * com.zyh432.cache.caffeine.init-cache-capacity
     */
    private Integer initCacheCapacity = 256;

    /**
     * 缓存最大容量，超过之后会按照(最近最少）策略进行缓存剔除
     * com.zyh432.cache.caffeine.max-cache-capacity
     */
    private Long maxCacheCapacity = 10000L;

    /**
     * 是否允许空值null作为缓存的value
     * com.zyh432.cache.caffeine.allow-null-value
     */
    private Boolean allowNullValue = Boolean.TRUE;
}
