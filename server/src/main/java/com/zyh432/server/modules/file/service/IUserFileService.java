package com.zyh432.server.modules.file.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zyh432.server.modules.file.context.*;
import com.zyh432.server.modules.file.entity.DatastorageplatformUserFile;
import com.zyh432.server.modules.file.vo.*;

import java.util.List;

/**
* @author 790585941
* @description 针对表【datastorageplatform_user_file(用户文件信息表)】的数据库操作Service
* @createDate 2024-01-08 22:25:23
*/
public interface IUserFileService extends IService<DatastorageplatformUserFile> {
    /**
     * 创建文件夹信息
     * @param createFolderContext
     * @return
     */
    Long createFolder(CreateFolderContext createFolderContext);

    /**
     * 查询用户的根文件夹信息
     * @param userId
     * @return
     */
    DatastorageplatformUserFile getUserRootFile(Long userId);

    /**
     *查询用户的文件列表
     * @param context
     * @return
     */
    List<DataStoragePlatformUserFileVO> getFileList(QueryFileListContext context);

    /**
     * 更新文件名称
     * @param context
     */
    void updateFilename(UpdateFilenameContext context);

    /**
     * 批量删除用户文件
     * @param context
     */
    void deleteFile(DeleteFileContext context);

    /**
     * 文件秒传
     * @param context
     * @return
     */
    boolean secUpload(SecUploadFileContext context);

    /**
     * 单文件上传
     * @param context
     */
    void upload(FileUploadContext context);

    /**
     * 文件分片上传
     * @param context
     * @return
     */
    FileChunkUploadVO chunkUpload(FileChunkUploadContext context);

    /**
     * 查询用户已上传的分片列表
     * @param context
     * @return
     */
    UploadedChunksVO getUploadedChunks(QueryUploadedChunksContext context);

    /**
     * 文件分片合并
     * @param context
     */
    void mergeFile(FileChunkMergeContext context);

    /**
     * 文件下载
     * @param context
     */
    void download(FileDownloadContext context);

    /**
     * 文件预览
     * @param context
     */
    void preview(FilePreviewContext context);

    /**
     * 查询用户的文件夹树
     * @param context
     * @return
     */
    List<FolderTreeNodeVO> getFolderTree(QueryFolderTreeContext context);

    /**
     * 文件转移
     * @param context
     */
    void transfer(TransferFileContext context);

    /**
     * 文件复制
     * @param context
     */
    void copy(CopyFileContext context);

    /**
     * 文件列表搜索
     * @param context
     * @return
     */
    List<FileSearchResultVO> search(FileSearchContext context);

    /**
     * 获取面包屑列表
     *
     * @param context
     * @return
     */
    List<BreadcrumbVO> getBreadcrumbs(QueryBreadcrumbsContext context);

    /**
     * 递归查询所有的子文件信息
     * @param records
     * @return
     */
    List<DatastorageplatformUserFile> findAllFileRecords(List<DatastorageplatformUserFile> records);

    /**
     * 递归查询所有的子文件信息
     * @param shareFileIdList
     * @return
     */
    List<DatastorageplatformUserFile> findAllFileRecordsByFileIdList(List<Long> shareFileIdList);

    /**
     * 实体转换
     *
     * @param records
     * @return
     */
    List<DataStoragePlatformUserFileVO> transferVOList(List<DatastorageplatformUserFile> records);

    /**
     * 文件下载 不校验用户是否是否是上传用户
     * @param fileDownloadContext
     */
    void downloadWithoutCheckUser(FileDownloadContext fileDownloadContext);
}
