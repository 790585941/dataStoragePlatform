package com.zyh432.server.common.utils;

import com.zyh432.core.constants.DataStoragePlatformConstants;

import java.util.Objects;

public class UserIdUtil {

    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 设置当前线程的用户ID
     *
     * @param userId
     */
    public static void set(Long userId) {
        threadLocal.set(userId);
    }

    /**
     * 获取当前线程的用户ID
     *
     * @return
     */
    public static Long get() {
        Long userId = threadLocal.get();
        if (Objects.isNull(userId)) {
            return DataStoragePlatformConstants.ZERO_LONG;
        }
        return userId;
    }

}
