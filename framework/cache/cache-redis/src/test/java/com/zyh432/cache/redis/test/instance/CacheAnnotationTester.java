package com.zyh432.cache.redis.test.instance;

import com.zyh432.cache.core.constants.CacheConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * Cache注解测试实体
 */
@Component
@Slf4j
public class CacheAnnotationTester {

    /**
     * 测试自适应缓存注解
     *
     * @param name
     * @return
     */
    @Cacheable(cacheNames = CacheConstants.dataStoragePlatform_CACHE_NAME, key = "#name", sync = true)
    public String testCacheable(String name) {
        log.info("call com.zyh432.cache.caffeine.test.instance.CacheAnnotationTester.testCacheable, param is {}", name);
        return new StringBuilder("hello ").append(name).toString();
    }

}
