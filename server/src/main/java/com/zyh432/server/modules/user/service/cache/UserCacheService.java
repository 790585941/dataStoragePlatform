package com.zyh432.server.modules.user.service.cache;

import com.zyh432.cache.core.constants.CacheConstants;
import com.zyh432.server.common.cache.AnnotationCacheService;
import com.zyh432.server.modules.user.entity.DatastorageplatformUser;
import com.zyh432.server.modules.user.mapper.DatastorageplatformUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.xml.ws.Action;
import java.io.Serializable;

/**
 * 用户模块缓存业务处理类
 */
@Component(value = "userAnnotationCacheService")
public class UserCacheService implements AnnotationCacheService<DatastorageplatformUser> {
    @Autowired
    private DatastorageplatformUserMapper mapper;

    /**
     * 根据ID查询实体
     * @param id
     * @return
     */
    @Cacheable(cacheNames = CacheConstants.dataStoragePlatform_CACHE_NAME,
    keyGenerator = "userIdKeyGenerator",sync = true)
    @Override
    public DatastorageplatformUser getById(Serializable id) {
        return mapper.selectById(id);
    }

    /**
     * 根据ID来更新缓存信息
     * @param id
     * @param entity
     * @return
     */
    @CacheEvict(cacheNames = CacheConstants.dataStoragePlatform_CACHE_NAME,
            keyGenerator = "userIdKeyGenerator")
    @Override
    public boolean updateById(Serializable id, DatastorageplatformUser entity) {
        return mapper.updateById(entity)==1;
    }

    /**
     * 根据ID来删除缓存信息
     * @param id
     * @return
     */
    @CacheEvict(cacheNames = CacheConstants.dataStoragePlatform_CACHE_NAME,
            keyGenerator = "userIdKeyGenerator")
    @Override
    public boolean removeById(Serializable id) {
        return mapper.deleteById(id)==1;
    }
}
