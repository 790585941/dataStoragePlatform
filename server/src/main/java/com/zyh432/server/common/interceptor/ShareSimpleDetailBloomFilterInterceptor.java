package com.zyh432.server.common.interceptor;

import com.zyh432.bloom.filter.core.BloomFilter;
import com.zyh432.bloom.filter.core.BloomFilterManager;
import com.zyh432.core.exception.DataStoragePlatformBusinessException;
import com.zyh432.core.response.ResponseCode;
import com.zyh432.core.utils.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@Slf4j
@Component
public class ShareSimpleDetailBloomFilterInterceptor implements BloomFilterInterceptor{
    @Autowired
    private BloomFilterManager manager;

    private static final String BLOOM_FILTER_NAME = "SHARE_SIMPLE_DETAIL";

    /**
     * 拦截器的名称
     * @return
     */
    @Override
    public String getName() {
        return "ShareSimpleDetailBloomFilterInterceptor";
    }
    /**
     * 要拦截的URI的集合
     * @return
     */
    @Override
    public String[] getPathPatterns() {
        return ArrayUtils.toArray("/share/simple");
    }
    /**
     * 不拦截的URI的集合
     * @return
     */
    @Override
    public String[] getExcludePatterns() {
        return new String[0];
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String encShareId = request.getParameter("shareId");
        if (StringUtils.isBlank(encShareId)){
            throw new DataStoragePlatformBusinessException("分享ID不能为空");
        }
        BloomFilter bloomFilter = manager.getFilter(BLOOM_FILTER_NAME);
        if (Objects.isNull(bloomFilter)){
            log.info("the bloomFilter named {} is null,give up existence judgment...",
                    BLOOM_FILTER_NAME);
            return true;
        }
        Long shareId = IdUtil.decrypt(encShareId);
        boolean mightContain = bloomFilter.mightContain(shareId);
        if (mightContain){
            log.info("the bloomFilter named {} judge shareId {} mightContain pass...",
                    BLOOM_FILTER_NAME,shareId);
            return true;
        }
        log.info("the bloomFilter named {} judge shareId {} mightContain fail...",
                BLOOM_FILTER_NAME,shareId);
        throw new DataStoragePlatformBusinessException(ResponseCode.SHARE_CANCELLED);
    }
}
