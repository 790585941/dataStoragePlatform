package com.zyh432.lock.test.instance;

import com.zyh432.lock.core.annotation.Lock;
import org.springframework.stereotype.Component;

/**
 * lock测试实例
 */
@Component
public class LockTester {
    @Lock(name = "test",keys = "#name",expireSecond = 10L)
    public String testLock(String name) {
        System.out.println(Thread.currentThread().getName()+" get the lock.");
        String result="hello "+name;
        System.out.println(Thread.currentThread().getName()+" release the lock.");
        return result;
    }
}
