package com.zyh432.server.modules.file.service;

import com.zyh432.server.modules.file.context.FileChunkSaveContext;
import com.zyh432.server.modules.file.entity.DatastorageplatformFileChunk;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 790585941
* @description 针对表【datastorageplatform_file_chunk(文件分片信息表)】的数据库操作Service
* @createDate 2024-01-08 22:25:23
*/
public interface IFileChunkService extends IService<DatastorageplatformFileChunk> {

    /**
     * 文件分片保存
     * @param context
     */
    void saveChunkFile(FileChunkSaveContext context);
}
