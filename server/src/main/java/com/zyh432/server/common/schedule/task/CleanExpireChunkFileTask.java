package com.zyh432.server.common.schedule.task;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zyh432.core.constants.DataStoragePlatformConstants;
import com.zyh432.schedule.ScheduleTask;
import com.zyh432.server.modules.file.entity.DatastorageplatformFileChunk;
import com.zyh432.server.modules.file.service.IFileChunkService;
import com.zyh432.storage.engine.core.StorageEngine;
import com.zyh432.storage.engine.core.context.DeleteFileContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import com.zyh432.server.common.event.log.ErrorLogEvent;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 过期分片清理任务
 */
@Component
@Slf4j
public class CleanExpireChunkFileTask implements ScheduleTask, ApplicationContextAware {

    private static final Long BATCH_SIZE = 500L;

    @Autowired
    private IFileChunkService iFileChunkService;

    @Autowired
    private StorageEngine storageEngine;

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 获取定时任务的名称
     *
     * @return
     */
    @Override
    public String getName() {
        return "CleanExpireChunkFileTask";
    }

    /**
     * 执行清理任务
     * <p>
     * 1、滚动查询过期的文件分片
     * 2、删除物理文件（委托文件存储引擎去实现）
     * 3、删除过期文件分片的记录信息
     * 4、重置上次查询的最大文件分片记录ID，继续滚动查询
     */
    @Override
    public void run() {
        log.info("{} start clean expire chunk file...", getName());

        List<DatastorageplatformFileChunk> expireFileChunkRecords;
        Long scrollPointer = 1L;

        do {
            expireFileChunkRecords = scrollQueryExpireFileChunkRecords(scrollPointer);
            if (CollectionUtils.isNotEmpty(expireFileChunkRecords)) {
                deleteRealChunkFiles(expireFileChunkRecords);
                List<Long> idList = deleteChunkFileRecords(expireFileChunkRecords);
                scrollPointer = Collections.max(idList);
            }
        } while (CollectionUtils.isNotEmpty(expireFileChunkRecords));

        log.info("{} finish clean expire chunk file...", getName());
    }

    /*********************************************private*********************************************/

    /**
     * 滚动查询过期的文件分片记录
     *
     * @param scrollPointer
     * @return
     */
    private List<DatastorageplatformFileChunk> scrollQueryExpireFileChunkRecords(Long scrollPointer) {
        QueryWrapper queryWrapper = Wrappers.query();
        queryWrapper.le("expiration_time", new Date());
        queryWrapper.ge("id", scrollPointer);
        queryWrapper.last(" limit " + BATCH_SIZE);
        return iFileChunkService.list(queryWrapper);
    }

    /**
     * 物理删除过期的文件分片文件实体
     *
     * @param expireFileChunkRecords
     */
    private void deleteRealChunkFiles(List<DatastorageplatformFileChunk> expireFileChunkRecords) {
        DeleteFileContext deleteFileContext = new DeleteFileContext();
        List<String> realPaths = expireFileChunkRecords.stream().map(DatastorageplatformFileChunk::getRealPath).collect(Collectors.toList());
        deleteFileContext.setRealFilePathList(realPaths);
        try {
            storageEngine.delete(deleteFileContext);
        } catch (IOException e) {
            saveErrorLog(realPaths);
        }
    }

    /**
     * @param realPaths
     */
    private void saveErrorLog(List<String> realPaths) {
        ErrorLogEvent event = new ErrorLogEvent(this, "文件物理删除失败，请手动执行文件删除！文件路径为：" + JSON.toJSONString(realPaths), DataStoragePlatformConstants.ZERO_LONG);
        applicationContext.publishEvent(event);
    }

    /**
     * 删除过期文件分片记录
     *
     * @param expireFileChunkRecords
     * @return
     */
    private List<Long> deleteChunkFileRecords(List<DatastorageplatformFileChunk> expireFileChunkRecords) {
        List<Long> idList = expireFileChunkRecords.stream().map(DatastorageplatformFileChunk::getId).collect(Collectors.toList());
        iFileChunkService.removeByIds(idList);
        return idList;
    }

}

