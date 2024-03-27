package com.zyh432.lock.core.key;

import com.google.common.collect.Maps;
import com.zyh432.core.utils.SpElUtil;
import com.zyh432.lock.core.LockContext;
import com.zyh432.lock.core.annotation.Lock;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Map;


/**
 * 锁的key生成器的公用父类
 */
public abstract class AbstractKeyGenerator implements KeyGenerator{
    /**
     * 生成锁的key
     * @param lockContext
     * @return
     */
    @Override
    public String generateKey(LockContext lockContext) {
        Lock annotation = lockContext.getAnnotation();
        String[] keys = annotation.keys();
        Map<String,String> keyValueMap= Maps.newHashMap();
        if (ArrayUtils.isNotEmpty(keys)){
            Arrays.stream(keys).forEach(key ->{
                keyValueMap.put(key, SpElUtil.getStringValue(key,lockContext.getClassName(),
                        lockContext.getMethodName(),lockContext.getClassType(),
                        lockContext.getMethod(),lockContext.getArgs(),lockContext.getParameterTypes(),
                        lockContext.getTarget()));
            });
        }
        return doGenerateKey(lockContext,keyValueMap);

    }

    /**
     * 具体逻辑下沉到子类去实现
     * @param lockContext
     * @param keyValueMap
     * @return
     */
    protected abstract String doGenerateKey(LockContext lockContext, Map<String, String> keyValueMap);


}
