package com.zyh432.server.common.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 布隆过滤器拦截器顶级接口
 */
public interface BloomFilterInterceptor extends HandlerInterceptor {
    /**
     * 拦截器的名称
     * @return
     */
    String getName();

    /**
     * 要拦截的URI的集合
     * @return
     */
    String[] getPathPatterns();

    /**
     * 不拦截的URI的集合
     * @return
     */
    String[] getExcludePatterns();
}
