package com.zyh432.server.modules.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.zyh432.core.exception.DataStoragePlatformBusinessException;
import com.zyh432.core.utils.FileUtil;
import com.zyh432.core.utils.IdUtil;
import com.zyh432.server.common.event.log.ErrorLogEvent;
import com.zyh432.server.modules.file.context.FileChunkMergeAndSaveContext;
import com.zyh432.server.modules.file.context.FileSaveContext;
import com.zyh432.server.modules.file.entity.DatastorageplatformFile;
import com.zyh432.server.modules.file.entity.DatastorageplatformFileChunk;
import com.zyh432.server.modules.file.service.IFileChunkService;
import com.zyh432.server.modules.file.service.IFileService;
import com.zyh432.server.modules.file.mapper.DatastorageplatformFileMapper;
import com.zyh432.storage.engine.core.StorageEngine;
import com.zyh432.storage.engine.core.context.DeleteFileContext;
import com.zyh432.storage.engine.core.context.MergeFileContext;
import com.zyh432.storage.engine.core.context.StoreFileContext;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author 790585941
* @description 针对表【datastorageplatform_file(物理文件信息表)】的数据库操作Service实现
* @createDate 2024-01-08 22:25:23
*/
@Service
public class IFileServiceImpl extends ServiceImpl<DatastorageplatformFileMapper, DatastorageplatformFile>
    implements IFileService , ApplicationContextAware {

    @Autowired
    private StorageEngine storageEngine;

    private ApplicationContext applicationContext;

    @Autowired
    private IFileChunkService iFileChunkService;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 上传单文件并保存实体记录
     * 1、上传单文件
     * 2、保存实体记录
     * @param context
     */
    @Override
    public void saveFile(FileSaveContext context) {
        storeMultipartFile(context);
        DatastorageplatformFile record = doSaveFile(context.getFilename(),
                context.getRealPath(),
                context.getTotalSize(),
                context.getIdentifier(),
                context.getUserId());
        context.setRecord(record);
    }

    /**
     * 合并物理文件并保存物理文件记录
     * 1、委托文件存储引擎合并文件分片
     * 2、保存物理文件记录
     * @param context
     */
    @Override
    public void mergeFileChunkAndSaveFile(FileChunkMergeAndSaveContext context) {
        doMergeFileChunk(context);
        DatastorageplatformFile record = doSaveFile(context.getFilename(), context.getRealPath(), context.getTotalSize(), context.getIdentifier(), context.getUserId());
        context.setRecord(record);
    }



    /****************************************private **********************************/

    /**
     * 委托文件存储引擎合并文件分片
     * 1、查询文件分片的记录
     * 2、根据文件分片的记录去合并物理文件
     * 3、删除文件分片记录
     * 4、封装合并文件的真实存储路径到上下文信息中
     * @param context
     */
    private void doMergeFileChunk(FileChunkMergeAndSaveContext context) {
        QueryWrapper<DatastorageplatformFileChunk> queryWrapper = Wrappers.query();
        queryWrapper.eq("identifier", context.getIdentifier());
        queryWrapper.eq("create_user", context.getUserId());
        queryWrapper.ge("expiration_time", new Date());
        List<DatastorageplatformFileChunk> chunkRecoredList = iFileChunkService.list(queryWrapper);
        if (CollectionUtils.isEmpty(chunkRecoredList)) {
            throw new DataStoragePlatformBusinessException("该文件未找到分片记录");
        }
        List<String> realPathList = chunkRecoredList.stream()
                .sorted(Comparator.comparing(DatastorageplatformFileChunk::getChunkNumber))
                .map(DatastorageplatformFileChunk::getRealPath)
                .collect(Collectors.toList());

        try {
            MergeFileContext mergeFileContext = new MergeFileContext();
            mergeFileContext.setFilename(context.getFilename());
            mergeFileContext.setIdentifier(context.getIdentifier());
            mergeFileContext.setUserId(context.getUserId());
            mergeFileContext.setRealPathList(realPathList);
            storageEngine.mergeFile(mergeFileContext);
            context.setRealPath(mergeFileContext.getRealPath());
        } catch (IOException e) {
            e.printStackTrace();
            throw new DataStoragePlatformBusinessException("文件分片合并失败");
        }

        List<Long> fileChunkRecordIdList = chunkRecoredList.stream().map(DatastorageplatformFileChunk::getId).collect(Collectors.toList());
        iFileChunkService.removeByIds(fileChunkRecordIdList);
    }



    /**
     * 上传单文件
     * 该方法委托文件存储引擎实现
     * @param context
     */
    private void storeMultipartFile(FileSaveContext context) {
        try {
            StoreFileContext storeFileContext = new StoreFileContext();
            storeFileContext.setInputStream(context.getFile().getInputStream());
            storeFileContext.setFilename(context.getFilename());
            storeFileContext.setTotalSize(context.getTotalSize());
            storageEngine.store(storeFileContext);
            context.setRealPath(storeFileContext.getRealPath());
        } catch (IOException e) {
            e.printStackTrace();
            throw new DataStoragePlatformBusinessException("文件上传失败");
        }

    }

    /**
     * 保存实体文件记录
     * @param filename
     * @param realPath
     * @param totalSize
     * @param identifier
     * @param userId
     * @return
     */
    private DatastorageplatformFile doSaveFile(String filename, String realPath, Long totalSize, String identifier, Long userId) {
        DatastorageplatformFile record=aseembleDatastorageplatformFile(filename, realPath, totalSize, identifier, userId);
        if (!save(record)) {
            try {
                DeleteFileContext deleteFileContext = new DeleteFileContext();
                deleteFileContext.setRealFilePathList(Lists.newArrayList(realPath));
                storageEngine.delete(deleteFileContext);
            } catch (IOException e) {
                e.printStackTrace();
                ErrorLogEvent errorLogEvent = new ErrorLogEvent(this, "文件物理删除失败，请执行手动删除！文件路径: " + realPath, userId);
                applicationContext.publishEvent(errorLogEvent);
            }
        }
        return record;
    }

    /**
     * 拼装文件实体对象
     * @param filename
     * @param realPath
     * @param totalSize
     * @param identifier
     * @param userId
     * @return
     */
    private DatastorageplatformFile aseembleDatastorageplatformFile(String filename, String realPath, Long totalSize, String identifier, Long userId) {
        DatastorageplatformFile record = new DatastorageplatformFile();

        record.setFileId(IdUtil.get());
        record.setFilename(filename);
        record.setRealPath(realPath);
        record.setFileSize(String.valueOf(totalSize));
        record.setFileSizeDesc(FileUtil.byteCountToDisplaySize(totalSize));
        record.setFileSuffix(FileUtil.getFileSuffix(filename));
        record.setIdentifier(identifier);
        record.setCreateUser(userId);
        record.setCreateTime(new Date());

        return record;

    }
}




