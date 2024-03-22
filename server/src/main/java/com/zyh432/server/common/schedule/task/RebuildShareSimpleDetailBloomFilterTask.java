package com.zyh432.server.common.schedule.task;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zyh432.bloom.filter.core.BloomFilter;
import com.zyh432.bloom.filter.core.BloomFilterManager;
import com.zyh432.core.constants.DataStoragePlatformConstants;
import com.zyh432.schedule.ScheduleTask;
import com.zyh432.server.common.event.log.ErrorLogEvent;
import com.zyh432.server.modules.file.entity.DatastorageplatformFileChunk;
import com.zyh432.server.modules.file.service.IFileChunkService;
import com.zyh432.server.modules.share.service.IShareService;
import com.zyh432.storage.engine.core.StorageEngine;
import com.zyh432.storage.engine.core.context.DeleteFileContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 定时重建简单分享详情布隆过滤器任务
 */
@Component
@Slf4j
public class RebuildShareSimpleDetailBloomFilterTask implements ScheduleTask {

    @Autowired
    private BloomFilterManager manager;
    @Autowired
    private IShareService iShareService;

    private static final String BLOOM_FILTER_NAME = "SHARE_SIMPLE_DETAIL";



    /**
     * 获取定时任务的名称
     *
     * @return
     */
    @Override
    public String getName() {
        return "RebuildShareSimpleDetailBloomFilterTask";
    }

    /**
     * 执行重建任务
     */
    @Override
    public void run() {
        log.info("start rebuild ShareSimpleDetailBloomFilter...");
        BloomFilter<Long> bloomFilter = manager.getFilter(BLOOM_FILTER_NAME);
        if (Objects.isNull(bloomFilter)){
            log.info("the bloomFilter named {} is null,give up rebuild...",BLOOM_FILTER_NAME);
            return;
        }
        bloomFilter.clear();

        Long startId=0L;
        Long limit=10000L;
        AtomicLong addCount=new AtomicLong(0L);

        List<Long> shareIdList;
        do{
            shareIdList=iShareService.rollingQueryShareId(startId,limit);
            if (CollectionUtils.isNotEmpty(shareIdList)){
                shareIdList.stream().forEach(shareId->{
                    bloomFilter.put(shareId);
                    addCount.incrementAndGet();
                });
                startId=shareIdList.get(shareIdList.size()-1);
            }
        }while(CollectionUtils.isNotEmpty(shareIdList));

        log.info("finish rebuild ShareSimpleDetailBloomFilter,total set item count {}...",
                addCount.get());
    }


}

