package com.zyh432.cache.caffeine.test.instance;

import lombok.extern.slf4j.Slf4j;
import com.zyh432.cache.core.constants.CacheConstants;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CacheAnnotationTester {

    /**
     * 测试自适应缓存注解
     *
     * @param name
     * @return
     */
    //sync = true: 表示缓存的读写操作是同步的，即只有一个线程能够进入方法并执行，以避免并发问题
    @Cacheable(cacheNames = CacheConstants.dataStoragePlatform_CACHE_NAME, key = "#name", sync = true)
    public String testCacheable(String name) {
        log.info("call com.zyh432.cache.caffeine.test.instance.CacheAnnotationTester.testCacheable, param is {}", name);
        return new StringBuilder("hello ").append(name).toString();
    }

}

