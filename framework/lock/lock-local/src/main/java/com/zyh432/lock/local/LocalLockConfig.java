package com.zyh432.lock.local;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.support.locks.DefaultLockRegistry;
import org.springframework.integration.support.locks.LockRegistry;

/**
 * 本地锁配置类
 */
@SpringBootConfiguration
@Slf4j
public class LocalLockConfig {
    /**
     * 创建本地锁注册器
     * @return 本地锁
     */
    @Bean
    public LockRegistry localLockRegistry() {
        LockRegistry lockRegistry = new DefaultLockRegistry();
        log.info("the local lock is loaded successfully!");
        return lockRegistry;
    }
}
