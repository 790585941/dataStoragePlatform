package com.zyh432.server.common.listenner.share;

import com.zyh432.server.common.event.file.DeleteFileEvent;
import com.zyh432.server.common.event.file.FileRestoreEvent;
import com.zyh432.server.modules.file.entity.DatastorageplatformUserFile;
import com.zyh432.server.modules.file.enums.DelFlagEnum;
import com.zyh432.server.modules.file.service.IUserFileService;
import com.zyh432.server.modules.share.service.IShareService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 监听文件状态变更导致分享状态变更的处理器
 */
@Component
public class ShareStatusChangeListener {

    @Autowired
    private IUserFileService iUserFileService;

    @Autowired
    private IShareService iShareService;

    /**
     * 监听文件被删除之后，刷新所有受影响的分享的状态
     *
     * @param event
     */
    @Async(value = "eventListenerTaskExecutor")
    @EventListener(DeleteFileEvent.class)
    public void changeShare2FileDeleted(DeleteFileEvent event) {
        List<Long> fileIdList = event.getFileIdList();
        if (CollectionUtils.isEmpty(fileIdList)) {
            return;
        }
        List<DatastorageplatformUserFile> allRecords = iUserFileService.findAllFileRecordsByFileIdList(fileIdList);
        List<Long> allAvailableFileIdList = allRecords.stream()
                .filter(record -> Objects.equals(record.getDelFlag(), DelFlagEnum.NO.getCode()))
                .map(DatastorageplatformUserFile::getFileId)
                .collect(Collectors.toList());
        allAvailableFileIdList.addAll(fileIdList);
        iShareService.refreshShareStatus(allAvailableFileIdList);
    }

    /**
     * 监听文件被还原后，刷新所有受影响的分享的状态
     *
     * @param event
     */
    @Async(value = "eventListenerTaskExecutor")
    @EventListener(FileRestoreEvent.class)
    public void changeShare2Normal(FileRestoreEvent event) {
        List<Long> fileIdList = event.getFileIdList();
        if (CollectionUtils.isEmpty(fileIdList)) {
            return;
        }
        List<DatastorageplatformUserFile> allRecords = iUserFileService.findAllFileRecordsByFileIdList(fileIdList);
        List<Long> allAvailableFileIdList = allRecords.stream()
                .filter(record -> Objects.equals(record.getDelFlag(), DelFlagEnum.NO.getCode()))
                .map(DatastorageplatformUserFile::getFileId)
                .collect(Collectors.toList());
        allAvailableFileIdList.addAll(fileIdList);
        iShareService.refreshShareStatus(allAvailableFileIdList);
    }

}
