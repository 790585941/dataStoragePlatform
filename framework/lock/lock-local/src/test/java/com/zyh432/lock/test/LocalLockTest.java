package com.zyh432.lock.test;

import com.zyh432.core.constants.DataStoragePlatformConstants;
import com.zyh432.lock.core.LockConstants;
import com.zyh432.lock.test.instance.LockTester;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@SpringBootTest(classes = LocalLockTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootApplication(scanBasePackages = DataStoragePlatformConstants.BASE_COMPONENT_SCAN_PATH
        +".lock")
public class LocalLockTest {
    @Autowired
    private LockRegistry lockRegistry;
    @Autowired
    private LockTester lockTester;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * 测试手动获取锁
     */
    @Test
    public void lockRegistryTest() throws InterruptedException {
        //启动十个线程去跑
        CountDownLatch countDownLatch=new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            threadPoolTaskExecutor.execute(()->{
                Lock lock = lockRegistry.obtain(LockConstants.DATASTORAGEPLATFORM_LOCK);
                boolean lockResult=false;
                try {
                    lockResult= lock.tryLock(60L, TimeUnit.SECONDS);
                    if (lockResult){
                        System.out.println(Thread.currentThread().getName()+" get the lock.");
                    }
                }catch (InterruptedException e){
                    throw new RuntimeException(e);
                }finally {
                    if (lockResult){
                        System.out.println(Thread.currentThread().getName()+" release the lock.");
                        lock.unlock();
                    }
                }
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();

    }

    /**
     * 测试锁注解
     * @throws InterruptedException
     */
    @Test
    public void lockTesterTest() throws InterruptedException {
        //启动十个线程
        CountDownLatch countDownLatch=new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            threadPoolTaskExecutor.execute(()->{
                lockTester.testLock("zyh");
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
    }
}
