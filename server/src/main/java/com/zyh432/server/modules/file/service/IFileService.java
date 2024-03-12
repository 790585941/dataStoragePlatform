package com.zyh432.server.modules.file.service;

import com.zyh432.server.modules.file.context.FileChunkMergeAndSaveContext;
import com.zyh432.server.modules.file.context.FileSaveContext;
import com.zyh432.server.modules.file.entity.DatastorageplatformFile;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 790585941
* @description 针对表【datastorageplatform_file(物理文件信息表)】的数据库操作Service
* @createDate 2024-01-08 22:25:23
*/
public interface IFileService extends IService<DatastorageplatformFile> {

    /**
     * 上传单文件并保存实体记录
     * @param fileSaveContext
     */
    void saveFile(FileSaveContext fileSaveContext);

    /**
     * 合并物理文件并保存物理文件记录
     * @param context
     */
    void mergeFileChunkAndSaveFile(FileChunkMergeAndSaveContext context);
}
