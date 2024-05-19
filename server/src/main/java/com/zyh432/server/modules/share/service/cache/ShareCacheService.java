package com.zyh432.server.modules.share.service.cache;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zyh432.server.common.cache.AbstractManualCacheService;
import com.zyh432.server.modules.share.entity.DatastorageplatformShare;
import com.zyh432.server.modules.share.entity.DatastorageplatformShareFile;
import com.zyh432.server.modules.share.mapper.DatastorageplatformShareMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 手动缓存实现分享业务的查询等操作
 */
@Component(value = "shareManualCacheService")
public class ShareCacheService extends AbstractManualCacheService<DatastorageplatformShare> {
    @Autowired
    private DatastorageplatformShareMapper mapper;

    @Override
    protected BaseMapper<DatastorageplatformShare> getBaseMapper() {
        return mapper;
    }

    /**
     * 获取缓存key的模板信息
     * @return
     */
    @Override
    public String getKeyFormat() {
        return "SHARE:ID:%s";
    }
}
