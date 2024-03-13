package com.zyh432.server.modules.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.zyh432.core.constants.DataStoragePlatformConstants;
import com.zyh432.core.exception.DataStoragePlatformBusinessException;
import com.zyh432.core.utils.FileUtil;
import com.zyh432.core.utils.IdUtil;
import com.zyh432.server.common.event.file.DeleteFileEvent;
import com.zyh432.server.common.event.search.UserSearchEvent;
import com.zyh432.server.common.utils.HttpUtil;
import com.zyh432.server.modules.file.constants.FileConstants;
import com.zyh432.server.modules.file.context.*;
import com.zyh432.server.modules.file.converter.FileConverter;
import com.zyh432.server.modules.file.entity.DatastorageplatformFile;
import com.zyh432.server.modules.file.entity.DatastorageplatformUserFile;
import com.zyh432.server.modules.file.enums.DelFlagEnum;
import com.zyh432.server.modules.file.enums.FileTypeEnum;
import com.zyh432.server.modules.file.enums.FolderFlagEnum;
import com.zyh432.server.modules.file.service.IFileChunkService;
import com.zyh432.server.modules.file.service.IFileService;
import com.zyh432.server.modules.file.service.IUserFileService;
import com.zyh432.server.modules.file.mapper.DatastorageplatformUserFileMapper;
import com.zyh432.server.modules.file.vo.*;
import com.zyh432.storage.engine.core.StorageEngine;
import com.zyh432.storage.engine.core.context.ReadFileContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import javax.xml.datatype.DatatypeConstants;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author 790585941
* @description 针对表【datastorageplatform_user_file(用户文件信息表)】的数据库操作Service实现
* @createDate 2024-01-08 22:25:23
*/
@Service(value = "userFileService")
public class UserFileServiceImpl extends ServiceImpl<DatastorageplatformUserFileMapper, DatastorageplatformUserFile>
    implements IUserFileService , ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Autowired
    private IFileService iFileService;

    @Autowired
    private FileConverter fileConverter;

    @Autowired
    private IFileChunkService iFileChunkService;

    @Autowired
    private StorageEngine storageEngine;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 创建文件夹信息
     * @param createFolderContext
     * @return
     */
    @Override
    public Long createFolder(CreateFolderContext createFolderContext) {

        return saveUserFile(createFolderContext.getParentId(),
                createFolderContext.getFolderName(),
                FolderFlagEnum.YES,
                null,
                null,
                createFolderContext.getUserId(),
                null);
    }

    /**
     * 查询用户的根文件夹信息
     * @param userId
     * @return
     */
    @Override
    public DatastorageplatformUserFile getUserRootFile(Long userId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("parent_id", FileConstants.TOP_PARENT_ID);
        queryWrapper.eq("del_flag", DelFlagEnum.NO.getCode());
        queryWrapper.eq("folder_flag", FolderFlagEnum.YES.getCode());
        return getOne(queryWrapper);
    }

    /**
     * 查询用户的文件列表
     * @param context
     * @return
     */
    @Override
    public List<DataStoragePlatformUserFileVO> getFileList(QueryFileListContext context) {
        return baseMapper.selectFileList(context);
    }

    /**
     * 更新文件名称
     * 1、检验更新文件名称的条件
     * 2、执行更新文件名称的操作
     * @param context
     */
    @Override
    public void updateFilename(UpdateFilenameContext context) {
        checkUpdateFilenameCondition(context);
        doUpdateFilename(context);
    }

    /**
     * 批量删除用户文件
     * 1、校验删除的条件
     * 2、执行批量删除的动作
     * 3、发布批量删除文件的事件，给其他模块订阅使用
     * @param context
     */
    @Override
    public void deleteFile(DeleteFileContext context) {
        checkFileDeleteCondition(context);
        doDeleteFile(context);
        afterFileDelete(context);
    }

    /**
     *文件秒传
     * 1、通过文件的唯一标识，查找对应的实体文件记录
     * 2、如果没有查到，直接返回秒传失败
     * 3、如果查到记录，直接挂载关联关系
     * @param context
     * @return
     */
    @Override
    public boolean secUpload(SecUploadFileContext context) {
        DatastorageplatformFile record= getFileByUserIdAndIdentifier(context.getUserId(),context.getIdentifier());
        if(Objects.isNull(record)){
            return false;
        }
        saveUserFile(context.getParentId(),
                context.getFilename(),
                FolderFlagEnum.NO,
                FileTypeEnum.getFileTypeCode(FileUtil.getFileSuffix(context.getFilename())),
                record.getFileId(),
                context.getUserId(),
                record.getFileSizeDesc());
        return true;
    }

    /**
     * 单文件上传
     * 1、上传文件并保存实体文件的记录
     * 2、保存用户文件的关系记录
     * @param context
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void upload(FileUploadContext context) {
        saveFile(context);
        saveUserFile(context.getParentId(),
                context.getFilename(),
                FolderFlagEnum.NO,
                FileTypeEnum.getFileTypeCode(FileUtil.getFileSuffix(context.getFilename())),
                context.getRecord().getFileId(),
                context.getUserId(),
                context.getRecord().getFileSizeDesc());
    }

    /**
     * 文件分片上传
     *1、上传实体文件
     * 2、保存分片文件记录
     * 3、校验是否全部分片上传完成
     * @param context
     * @return
     */
    @Override
    public FileChunkUploadVO chunkUpload(FileChunkUploadContext context) {
        FileChunkSaveContext fileChunkSaveContext = fileConverter.fileChunkUploadContext2FileChunkSaveContext(context);
        iFileChunkService.saveChunkFile(fileChunkSaveContext);
        FileChunkUploadVO vo = new FileChunkUploadVO();
        vo.setMergeFlag(fileChunkSaveContext.getMergeFlagEnum().getCode());
        return vo;
    }

    /**
     * 查询用户已上传的分片列表
     *
     * 1、查询已上传的分片列表
     * 2、封装返回实体
     * @param context
     * @return
     */
    @Override
    public UploadedChunksVO getUploadedChunks(QueryUploadedChunksContext context) {
        QueryWrapper queryWrapper = Wrappers.query();
        queryWrapper.select("chunk_number");
        queryWrapper.eq("identifier", context.getIdentifier());
        queryWrapper.eq("create_user", context.getUserId());
        queryWrapper.gt("expiration_time", new Date());

        List<Integer> uploadedChunks = iFileChunkService.listObjs(queryWrapper, value -> (Integer) value);

        UploadedChunksVO vo = new UploadedChunksVO();
        vo.setUploadedChunks(uploadedChunks);
        return vo;
    }

    /**
     * 文件分片合并
     * 1、文件分片物理合并
     * 2、保存文件实体记录
     * 3、保存文件用户关系映射
     * @param context
     */
    @Override
    public void mergeFile(FileChunkMergeContext context) {
        mergeFileChunkAndSaveFile(context);
        saveUserFile(context.getParentId(),
                context.getFilename(),
                FolderFlagEnum.NO,
                FileTypeEnum.getFileTypeCode(FileUtil.getFileSuffix(context.getFilename())),
                context.getRecord().getFileId(),
                context.getUserId(),
                context.getRecord().getFileSizeDesc());
    }

    /**
     * 文件下载
     * 1、参数校验：校验文件是否存在，文件是否属于该用户
     * 2、校验该文件是不是一个文件夹
     * 3、执行下载的动作
     * @param context
     */
    @Override
    public void download(FileDownloadContext context) {
        DatastorageplatformUserFile record = getById(context.getFileId());
        checkOperatePermission(record, context.getUserId());
        if (checkIsFolder(record)) {
            throw new DataStoragePlatformBusinessException("文件夹暂不支持下载");
        }
        doDownload(record, context.getResponse());
    }

    /**
     * 文件预览
     * 1、参数校验：校验文件是否存在，文件是否属于该用户
     * 2、校验该文件是不是一个文件夹
     * 3、执行预览的动作
     * @param context
     */
    @Override
    public void preview(FilePreviewContext context) {
        DatastorageplatformUserFile record = getById(context.getFileId());
        checkOperatePermission(record, context.getUserId());
        if (checkIsFolder(record)) {
            throw new DataStoragePlatformBusinessException("文件夹暂不支持下载");
        }
        doPreview(record, context.getResponse());
    }


    /**
     * 查询用户的文件夹树
     * 1、查询出该用户的所有文件夹列表
     * 2、在内存中拼装文件夹树
     * @param context
     * @return
     */
    @Override
    public List<FolderTreeNodeVO> getFolderTree(QueryFolderTreeContext context) {
        List<DatastorageplatformUserFile> folderRecords = queryFolderRecords(context.getUserId());
        List<FolderTreeNodeVO> result = assembleFolderTreeNodeVOList(folderRecords);
        return result;
    }

    /**
     * 文件转移
     * 1、权限校验
     * 2、执行动作
     * @param context
     */
    @Override
    public void transfer(TransferFileContext context) {
        checkTransferCondition(context);
        doTransfer(context);
    }

    /**
     * 文件复制
     * <p>
     * 1、条件校验
     * 2、执行动作
     *
     * @param context
     */
    @Override
    public void copy(CopyFileContext context) {
        checkCopyCondition(context);
        doCopy(context);
    }


    /**
     * 文件列表搜索
     * <p>
     * 1、执行文件搜索
     * 2、拼装文件的父文件夹名称
     * 3、执行文件搜索后的后置动作
     *
     * @param context
     * @return
     */
    @Override
    public List<FileSearchResultVO> search(FileSearchContext context) {
        List<FileSearchResultVO> result = doSearch(context);
        fillParentFilename(result);
        afterSearch(context);
        return result;
    }


    /**
     * 获取面包屑列表
     * <p>
     * 1、获取用户所有文件夹信息
     * 2、拼接需要用到的面包屑的列表
     *
     * @param context
     * @return
     */
    @Override
    public List<BreadcrumbVO> getBreadcrumbs(QueryBreadcrumbsContext context) {
        List<DatastorageplatformUserFile> folderRecords = queryFolderRecords(context.getUserId());
        Map<Long, BreadcrumbVO> prepareBreadcrumbVOMap = folderRecords.stream().map(BreadcrumbVO::transfer).collect(Collectors.toMap(BreadcrumbVO::getId, a -> a));
        BreadcrumbVO currentNode;
        Long fileId = context.getFileId();
        List<BreadcrumbVO> result = Lists.newLinkedList();
        do {
            currentNode = prepareBreadcrumbVOMap.get(fileId);
            if (Objects.nonNull(currentNode)) {
                result.add(0, currentNode);
                fileId = currentNode.getParentId();
            }
        } while (Objects.nonNull(currentNode));
        return result;
    }


    /**
     * 递归查询所有的子文件信息
     *
     * @param records
     * @return
     */
    @Override
    public List<DatastorageplatformUserFile> findAllFileRecords(List<DatastorageplatformUserFile> records) {
        List<DatastorageplatformUserFile> result = Lists.newArrayList(records);
        if (org.apache.commons.collections.CollectionUtils.isEmpty(result)) {
            return result;
        }
        long folderCount = result.stream().filter(record -> Objects.equals(record.getFolderFlag(), FolderFlagEnum.YES.getCode())).count();
        if (folderCount == 0) {
            return result;
        }
        records.stream().forEach(record -> doFindAllChildRecords(result, record));
        return result;
    }


    /****************************************private **********************************/

    /**
     * 递归查询所有的子文件列表
     * 忽略是否删除的标识
     *
     * @param result
     * @param record
     */
    private void doFindAllChildRecords(List<DatastorageplatformUserFile> result, DatastorageplatformUserFile record) {
        if (Objects.isNull(record)) {
            return;
        }
        if (!checkIsFolder(record)) {
            return;
        }
        List<DatastorageplatformUserFile> childRecords = findChildRecordsIgnoreDelFlag(record.getFileId());
        if (org.apache.commons.collections.CollectionUtils.isEmpty(childRecords)) {
            return;
        }
        result.addAll(childRecords);
        childRecords.stream()
                .filter(childRecord -> FolderFlagEnum.YES.getCode().equals(childRecord.getFolderFlag()))
                .forEach(childRecord -> doFindAllChildRecords(result, childRecord));
    }

    /**
     * 查询文件夹下面的文件记录，忽略删除标识
     *
     * @param fileId
     * @return
     */
    private List<DatastorageplatformUserFile> findChildRecordsIgnoreDelFlag(Long fileId) {
        QueryWrapper queryWrapper = Wrappers.query();
        queryWrapper.eq("parent_id", fileId);
        List<DatastorageplatformUserFile> childRecords = list(queryWrapper);
        return childRecords;
    }

    /**
     * 搜索文件列表
     *
     * @param context
     * @return
     */
    private List<FileSearchResultVO> doSearch(FileSearchContext context) {
        return baseMapper.searchFile(context);
    }


    /**
     * 填充文件列表的父文件名称
     *
     * @param result
     */
    private void fillParentFilename(List<FileSearchResultVO> result) {
        if (org.apache.commons.collections.CollectionUtils.isEmpty(result)) {
            return;
        }
        List<Long> parentIdList = result.stream().map(FileSearchResultVO::getParentId).collect(Collectors.toList());
        List<DatastorageplatformUserFile> parentRecords = listByIds(parentIdList);
        Map<Long, String> fileId2filenameMap = parentRecords.stream().collect(Collectors.toMap(DatastorageplatformUserFile::getFileId, DatastorageplatformUserFile::getFilename));
        result.stream().forEach(vo -> vo.setParentFilename(fileId2filenameMap.get(vo.getParentId())));
    }


    /**
     * 搜索的后置操作
     * <p>
     * 1、发布文件搜索的事件
     *
     * @param context
     */
    private void afterSearch(FileSearchContext context) {
        UserSearchEvent event = new UserSearchEvent(this, context.getKeyword(), context.getUserId());
        applicationContext.publishEvent(event);
    }




    /**
     * 文件转移的条件校验
     * <p>
     * 1、目标文件必须是一个文件夹
     * 2、选中的要转移的文件列表中不能含有目标文件夹以及其子文件夹
     *
     * @param context
     */
    private void checkCopyCondition(CopyFileContext context) {
        Long targetParentId = context.getTargetParentId();
        if (!checkIsFolder(getById(targetParentId))) {
            throw new DataStoragePlatformBusinessException("目标文件不是一个文件夹");
        }
        List<Long> fileIdList = context.getFileIdList();
        List<DatastorageplatformUserFile> prepareRecords = listByIds(fileIdList);
        context.setPrepareRecords(prepareRecords);
        if (checkIsChildFolder(prepareRecords, targetParentId, context.getUserId())) {
            throw new DataStoragePlatformBusinessException("目标文件夹ID不能是选中文件列表的文件夹ID或其子文件夹ID");
        }
    }


    /**
     * 执行文件复制的动作
     *
     * @param context
     */
    private void doCopy(CopyFileContext context) {
        List<DatastorageplatformUserFile> prepareRecords = context.getPrepareRecords();
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(prepareRecords)) {
            List<DatastorageplatformUserFile> allRecords = Lists.newArrayList();
            prepareRecords.stream().forEach(record -> assembleCopyChildRecord(allRecords, record, context.getTargetParentId(), context.getUserId()));
            if (!saveBatch(allRecords)) {
                throw new DataStoragePlatformBusinessException("文件复制失败");
            }
        }
    }

    /**
     * 拼装当前文件记录以及所有的子文件记录
     *
     * @param allRecords
     * @param record
     * @param targetParentId
     * @param userId
     */
    private void assembleCopyChildRecord(List<DatastorageplatformUserFile> allRecords, DatastorageplatformUserFile record, Long targetParentId, Long userId) {
        Long newFileId = IdUtil.get();
        Long oldFileId = record.getFileId();

        record.setParentId(targetParentId);
        record.setFileId(newFileId);
        record.setUserId(userId);
        record.setCreateUser(userId);
        record.setCreateTime(new Date());
        record.setUpdateUser(userId);
        record.setUpdateTime(new Date());
        handleDuplicateFilename(record);

        allRecords.add(record);

        if (checkIsFolder(record)) {
            List<DatastorageplatformUserFile> childRecords = findChildRecords(oldFileId);
            if (org.apache.commons.collections.CollectionUtils.isEmpty(childRecords)) {
                return;
            }
            childRecords.stream().forEach(childRecord -> assembleCopyChildRecord(allRecords, childRecord, newFileId, userId));
        }

    }


    /**
     * 查找下一级的文件记录
     *
     * @param parentId
     * @return
     */
    private List<DatastorageplatformUserFile> findChildRecords(Long parentId) {
        QueryWrapper queryWrapper = Wrappers.query();
        queryWrapper.eq("parent_id", parentId);
        queryWrapper.eq("del_flag", DelFlagEnum.NO.getCode());
        return list(queryWrapper);
    }



    /**
     * 执行文件转移的动作
     *
     * @param context
     */
    private void doTransfer(TransferFileContext context) {
        List<DatastorageplatformUserFile> prepareRecords = context.getPrepareRecords();
        prepareRecords.stream().forEach(record -> {
            record.setParentId(context.getTargetParentId());
            record.setUserId(context.getUserId());
            record.setCreateUser(context.getUserId());
            record.setCreateTime(new Date());
            record.setUpdateUser(context.getUserId());
            record.setUpdateTime(new Date());
            handleDuplicateFilename(record);
        });
        if (!updateBatchById(prepareRecords)) {
            throw new DataStoragePlatformBusinessException("文件转移失败");
        }
    }

    /**
     * 文件转移的条件校验
     * 1、目标文件必须是一个文件夹
     * 2、选中的要转移的文件列表中不能含有目标文件夹以及其子文件夹
     *
     * @param context
     */
    private void checkTransferCondition(TransferFileContext context) {
        Long targetParentId = context.getTargetParentId();
        if (!checkIsFolder(getById(targetParentId))) {
            throw new DataStoragePlatformBusinessException("目标文件不是一个文件夹");
        }
        List<Long> fileIdList = context.getFileIdList();
        List<DatastorageplatformUserFile> prepareRecords = listByIds(fileIdList);
        context.setPrepareRecords(prepareRecords);
        if (checkIsChildFolder(prepareRecords, targetParentId, context.getUserId())) {
            throw new DataStoragePlatformBusinessException("目标文件夹ID不能是选中文件列表的文件夹ID或其子文件夹ID");
        }
    }

    /**
     * 校验目标文件夹ID是都是要操作的文件记录的文件夹ID以及其子文件夹ID
     * <p>
     * 1、如果要操作的文件列表中没有文件夹，那就直接返回false
     * 2、拼装文件夹ID以及所有子文件夹ID，判断存在即可
     *
     * @param prepareRecords
     * @param targetParentId
     * @param userId
     * @return
     */
    private boolean checkIsChildFolder(List<DatastorageplatformUserFile> prepareRecords, Long targetParentId, Long userId) {
        prepareRecords = prepareRecords.stream().filter(record -> Objects.equals(record.getFolderFlag(), FolderFlagEnum.YES.getCode())).collect(Collectors.toList());
        if (org.apache.commons.collections.CollectionUtils.isEmpty(prepareRecords)) {
            return false;
        }
        List<DatastorageplatformUserFile> folderRecords = queryFolderRecords(userId);
        Map<Long, List<DatastorageplatformUserFile>> folderRecordMap = folderRecords.stream().collect(Collectors.groupingBy(DatastorageplatformUserFile::getParentId));
        List<DatastorageplatformUserFile> unavailableFolderRecords = Lists.newArrayList();
        unavailableFolderRecords.addAll(prepareRecords);
        prepareRecords.stream().forEach(record -> findAllChildFolderRecords(unavailableFolderRecords, folderRecordMap, record));
        List<Long> unavailableFolderRecordIds = unavailableFolderRecords.stream().map(DatastorageplatformUserFile::getFileId).collect(Collectors.toList());
        return unavailableFolderRecordIds.contains(targetParentId);
    }


    /**
     * 查找文件夹的所有子文件夹记录
     *
     * @param unavailableFolderRecords
     * @param folderRecordMap
     * @param record
     */
    private void findAllChildFolderRecords(List<DatastorageplatformUserFile> unavailableFolderRecords, Map<Long, List<DatastorageplatformUserFile>> folderRecordMap, DatastorageplatformUserFile record) {
        if (Objects.isNull(record)) {
            return;
        }
        List<DatastorageplatformUserFile> childFolderRecords = folderRecordMap.get(record.getFileId());
        if (org.apache.commons.collections.CollectionUtils.isEmpty(childFolderRecords)) {
            return;
        }
        unavailableFolderRecords.addAll(childFolderRecords);
        childFolderRecords.stream().forEach(childRecord -> findAllChildFolderRecords(unavailableFolderRecords, folderRecordMap, childRecord));
    }


    /**
     * 拼装文件夹树列表
     * @param folderRecords
     * @return
     */
    private List<FolderTreeNodeVO> assembleFolderTreeNodeVOList(List<DatastorageplatformUserFile> folderRecords) {
        if (org.apache.commons.collections.CollectionUtils.isEmpty(folderRecords)) {
            return Lists.newArrayList();
        }
        List<FolderTreeNodeVO> mappedFolderTreeNodeVOList = folderRecords.stream().map(fileConverter::datastorageplatformUserFile2FolderTreeNodeVO).collect(Collectors.toList());
        //按照父节点进行分组，对于每个节点，将其子节点挂载到自己的children属性上
        Map<Long, List<FolderTreeNodeVO>> mappedFolderTreeNodeVOMap = mappedFolderTreeNodeVOList.stream().collect(Collectors.groupingBy(FolderTreeNodeVO::getParentId));
        for (FolderTreeNodeVO node : mappedFolderTreeNodeVOList) {
            List<FolderTreeNodeVO> children = mappedFolderTreeNodeVOMap.get(node.getId());
            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(children)) {
                node.getChildren().addAll(children);
            }
        }
        return mappedFolderTreeNodeVOList.stream().filter(node -> Objects.equals(node.getParentId(), FileConstants.TOP_PARENT_ID)).collect(Collectors.toList());

    }

    /**
     * 查询用户所有有效的文件夹信息
     *
     * @param userId
     * @return
     */
    private List<DatastorageplatformUserFile> queryFolderRecords(Long userId) {
        QueryWrapper queryWrapper = Wrappers.query();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("folder_flag", FolderFlagEnum.YES.getCode());
        queryWrapper.eq("del_flag", DelFlagEnum.NO.getCode());
        return list(queryWrapper);
    }


    /**
     *执行文件预览的动作
     1、查询文件的真实存储路径
     2、添加跨域的公共响应头
     3、委托文件存储引擎去读取文件内容到响应的输出流中
     * @param record
     * @param response
     */
    private void doPreview(DatastorageplatformUserFile record, HttpServletResponse response) {
        DatastorageplatformFile realFileRecord = iFileService.getById(record.getRealFileId());
        if (Objects.isNull(realFileRecord)) {
            throw new DataStoragePlatformBusinessException("当前的文件记录不存在");
        }
        addCommonResponseHeader(response, realFileRecord.getFilePreviewContentType());
        realFile2OutputStream(realFileRecord.getRealPath(), response);
    }


    /**
     * 执行文件下载的动作
     * 1、查询文件的真实存储路径
     * 2、添加跨域的公共响应头
     * 3、拼装下载文件的名称，长度等等响应信息
     * 4、委托文件存储引擎去读取文件内容到响应的输出流中
     * @param record
     * @param response
     */
    private void doDownload(DatastorageplatformUserFile record, HttpServletResponse response) {
        DatastorageplatformFile realFileRecord = iFileService.getById(record.getRealFileId());
        if (Objects.isNull(realFileRecord)) {
            throw new DataStoragePlatformBusinessException("当前的文件记录不存在");
        }
        addCommonResponseHeader(response, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        addDownloadAttribute(response, record, realFileRecord);
        realFile2OutputStream(realFileRecord.getRealPath(), response);
    }

    /**
     * 委托文件存储引擎去读取文件内容并写入到输出流中
     * @param realPath
     * @param response
     */
    private void realFile2OutputStream(String realPath, HttpServletResponse response) {
        try {
            ReadFileContext context = new ReadFileContext();
            context.setRealPath(realPath);
            context.setOutputStream(response.getOutputStream());
            storageEngine.realFile(context);
        } catch (IOException e) {
            e.printStackTrace();
            throw new DataStoragePlatformBusinessException("文件下载失败");
        }
    }

    /**
     * 添加文件下载的属性信息
     * @param response
     * @param record
     * @param realFileRecord
     */
    private void addDownloadAttribute(HttpServletResponse response, DatastorageplatformUserFile record, DatastorageplatformFile realFileRecord) {
        try {
            response.addHeader(FileConstants.CONTENT_DISPOSITION_STR,
                    FileConstants.CONTENT_DISPOSITION_VALUE_PREFIX_STR + new String(record.getFilename().getBytes(FileConstants.GB2312_STR), FileConstants.IOS_8859_1_STR));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new DataStoragePlatformBusinessException("文件下载失败");
        }
        response.setContentLengthLong(Long.valueOf(realFileRecord.getFileSize()));
    }

    /**
     * 添加公共的文件读取响应头
     *
     * @param response
     * @param contentTypeValue
     */
    private void addCommonResponseHeader(HttpServletResponse response, String contentTypeValue) {
        response.reset();
        HttpUtil.addCorsResponseHeaders(response);
        response.addHeader(FileConstants.CONTENT_TYPE_STR, contentTypeValue);
        response.setContentType(contentTypeValue);
    }

    /**
     *检验当前文件记录是不是一个文件夹
     * @param record
     * @return
     */
    private boolean checkIsFolder(DatastorageplatformUserFile record) {
        if (Objects.isNull(record)) {
            throw new DataStoragePlatformBusinessException("当前文件记录不存在");
        }
        return FolderFlagEnum.YES.getCode().equals(record.getFolderFlag());
    }

    /**
     * 校验用户的操作权限
     * 1、文件记录必须存在
     * 2、文件记录的创建者必须是该登录用户
     * @param record
     * @param userId
     */
    private void checkOperatePermission(DatastorageplatformUserFile record, Long userId) {
        if (Objects.isNull(record)) {
            throw new DataStoragePlatformBusinessException("当前文件记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new DataStoragePlatformBusinessException("您没有该文件的操作权限");
        }
    }


    /**
     * 合并文件分片并保存物理文件记录
     * @param context
     */
    private void mergeFileChunkAndSaveFile(FileChunkMergeContext context) {
        FileChunkMergeAndSaveContext fileChunkMergeAndSaveContext = fileConverter.fileChunkMergeContext2FileChunkMergeAndSaveContext(context);
        iFileService.mergeFileChunkAndSaveFile(fileChunkMergeAndSaveContext);
        context.setRecord(fileChunkMergeAndSaveContext.getRecord());
    }



    /**
     * 上传文件并保存实体文件记录
     * 委托给实体文件的Service去完成该操作
     * @param context
     */
    private void saveFile(FileUploadContext context) {
        FileSaveContext fileSaveContext = fileConverter.fileUploadContext2FileSaveContext(context);
        iFileService.saveFile(fileSaveContext);
        context.setRecord(fileSaveContext.getRecord());
    }


    /**
     *
     * @param userId
     * @param identifier
     * @return
     */
    private DatastorageplatformFile getFileByUserIdAndIdentifier(Long userId, String identifier) {
        QueryWrapper queryWrapper= Wrappers.query();
        queryWrapper.eq("create_user",userId);
        queryWrapper.eq("identifier",identifier);
        List<DatastorageplatformFile> records = iFileService.list(queryWrapper);
        if(CollectionUtils.isEmpty(records)){
            return null;
        }
        return records.get(DataStoragePlatformConstants.ZERO_INT);
    }
    /**
     * 文件删除的后置操作
     * 1、对外发布文件删除的事件
     * @param context
     */
    private void afterFileDelete(DeleteFileContext context) {
        DeleteFileEvent deleteFileEvent=new DeleteFileEvent(this,context.getFileIdList());
        applicationContext.publishEvent(deleteFileEvent);
    }

    /**
     * 执行文件删除的操作
     * @param context
     */
    private void doDeleteFile(DeleteFileContext context) {
        List<Long> fileIdList = context.getFileIdList();

        UpdateWrapper updateWrapper = new UpdateWrapper();
        updateWrapper.in("file_id", fileIdList);
        updateWrapper.set("del_flag", DelFlagEnum.YES.getCode());
        updateWrapper.set("update_time", new Date());

        if (!update(updateWrapper)) {
            throw new DataStoragePlatformBusinessException("文件删除失败");
        }
    }

    /**
     * 删除文件之前的前置校验
     * 1、文件ID合法校验
     * 2、用户有权限删除该文件
     * @param context
     */
    private void checkFileDeleteCondition(DeleteFileContext context) {
        List<Long> fileIdList = context.getFileIdList();

        List<DatastorageplatformUserFile> datastorageplatformUserFiles = listByIds(fileIdList);
        if(datastorageplatformUserFiles.size() != fileIdList.size()){
            throw new DataStoragePlatformBusinessException("存在不合法的文件记录");
        }
        Set<Long> fileIdSet = datastorageplatformUserFiles.stream().map(DatastorageplatformUserFile::getFileId).collect(Collectors.toSet());
        int oldSize = fileIdSet.size();
        fileIdSet.addAll(fileIdList);
        int newSize = fileIdSet.size();
        if(oldSize != newSize){
            throw new DataStoragePlatformBusinessException("存在不合法的文件记录");
        }

        Set<Long> userIdSet = datastorageplatformUserFiles.stream().map(DatastorageplatformUserFile::getUserId).collect(Collectors.toSet());
        if(userIdSet.size() != 1){
            throw new DataStoragePlatformBusinessException("存在不合法的文件记录");
        }
        Long dbUserId = userIdSet.stream().findFirst().get();
        if (!Objects.equals(dbUserId,context.getUserId())){
            throw new DataStoragePlatformBusinessException("当前登录用户没有删除该文件的权限");
        }
    }




    /**
     * 保存用户文件的映射记录
     * @param parentId
     * @param filename
     * @param folderFlagEnum
     * @param fileType
     * @param realFileId
     * @param userId
     * @param fileSizeDesc
     * @return
     */
    private Long saveUserFile(Long parentId,
                         String filename,
                         FolderFlagEnum folderFlagEnum,
                         Integer fileType,
                         Long realFileId,
                         Long userId,
                         String fileSizeDesc){
        DatastorageplatformUserFile entity=assembleDatastorageplatformUserFile(
                parentId, userId, filename, folderFlagEnum, fileType, realFileId, fileSizeDesc
        );
        if (!save((entity))){
            throw new DataStoragePlatformBusinessException("保存文件信息失败");
        }
        return entity.getFileId();
    }

    /**
     * 用户文件映射关系实体化
     * 1.构建并填充实体
     * 2.处理文件命名一致的问题
     * @param parentId
     * @param userId
     * @param filename
     * @param folderFlagEnum
     * @param fileType
     * @param realFileId
     * @param fileSizeDesc
     * @return
     */
    private DatastorageplatformUserFile assembleDatastorageplatformUserFile(Long parentId, Long userId, String filename, FolderFlagEnum folderFlagEnum, Integer fileType, Long realFileId, String fileSizeDesc) {
        DatastorageplatformUserFile entity=new DatastorageplatformUserFile();
        entity.setFileId(IdUtil.get());
        entity.setUserId(userId);
        entity.setParentId(parentId);
        entity.setRealFileId(realFileId);
        entity.setFilename(filename);
        entity.setFolderFlag(folderFlagEnum.getCode());
        entity.setFileSizeDesc(fileSizeDesc);
        entity.setFileType(fileType);
        entity.setDelFlag(DelFlagEnum.NO.getCode());
        entity.setCreateUser(userId);
        entity.setCreateTime(new Date());
        entity.setUpdateUser(userId);
        entity.setUpdateTime(new Date());
        handleDuplicateFilename(entity);
        return entity;
    }

    /**
     * 处理用户重复名称
     * 如果同一文件夹下面有文件名称重复
     * 按照系统级规则重命名文件
     * @param entity
     */
    private void handleDuplicateFilename(DatastorageplatformUserFile entity) {
        String filename=entity.getFilename(),
                newFilenameWithoutSuffix,
                newFilenameSuffix;
        int newFilenamePointPosition = filename.lastIndexOf(DataStoragePlatformConstants.POINT_STR);
        if (newFilenamePointPosition== DataStoragePlatformConstants.MINUS_ONE_INT){
            newFilenameWithoutSuffix=filename;
            newFilenameSuffix= StringUtils.EMPTY;
        }else{
            newFilenameWithoutSuffix=filename.substring(DataStoragePlatformConstants.ZERO_INT,newFilenamePointPosition);
            newFilenameSuffix=filename.replace(newFilenameWithoutSuffix, StringUtils.EMPTY);
        }
        int count= getDuplicateFilename(entity,newFilenameWithoutSuffix);
        if (count==0){
            return;
        }
        String newFilename=assembleNewFilename(newFilenameWithoutSuffix,count,newFilenameSuffix);
        entity.setFilename(newFilename);
    }

    /**
     * 拼装新文件名称
     * 拼装规则参考操作系统重复文件名称的重命名规范
     * @param newFilenameWithoutSuffix
     * @param count
     * @param newFilenameSuffix
     * @return
     */
    private String assembleNewFilename(String newFilenameWithoutSuffix, int count, String newFilenameSuffix) {
        String newFilename=new StringBuilder(newFilenameWithoutSuffix)
                .append(FileConstants.CN_LEFT_PARENTHESES_STR)
                .append(count)
                .append(FileConstants.CN_RIGHT_PARENTHESES_STR)
                .append(newFilenameSuffix)
                .toString();
        return newFilename;
    }

    /**
     * 查找同一文件夹下的同名文件数量
     * @param entity
     * @param newFilenameWithoutSuffix
     * @return
     */
    private int getDuplicateFilename(DatastorageplatformUserFile entity, String newFilenameWithoutSuffix) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("parent_id", entity.getParentId());
        queryWrapper.eq("folder_flag", entity.getFolderFlag());
        queryWrapper.eq("user_id", entity.getUserId());
        queryWrapper.eq("del_flag", DelFlagEnum.NO.getCode());
        queryWrapper.likeLeft("filename", newFilenameWithoutSuffix);
        return count(queryWrapper);

    }


    /**
     * 执行文件重命名的操作
     * @param context
     */
    private void doUpdateFilename(UpdateFilenameContext context) {
        DatastorageplatformUserFile entity = context.getEntity();
        entity.setFilename(context.getNewFilename());
        entity.setUpdateUser(context.getUserId());
        entity.setUpdateTime(new Date());
        if (!updateById(entity)){
            throw new DataStoragePlatformBusinessException("更新文件名称失败");
        }
    }

    /**
     * 更新文件名称的条件检验
     * 1、文件ID是有效的
     * 2、用户有权限更新该文件的文件名称
     * 3、新旧文件名称不能一样
     * 4、不能使用当前文件夹下面的子文件的名称
     * @param context
     */
    private void checkUpdateFilenameCondition(UpdateFilenameContext context) {
        Long fileId = context.getFileId();
        DatastorageplatformUserFile entity = getById(fileId);
        if (Objects.isNull(entity)){
            throw new DataStoragePlatformBusinessException("该文件ID无效");
        }
        if (!Objects.equals(entity.getUserId(),context.getUserId())){
            throw new DataStoragePlatformBusinessException("当前登录用户无权限更新该文件名称");
        }
        if(Objects.equals(entity.getFilename(),context.getNewFilename())){
            throw new DataStoragePlatformBusinessException("新旧文件名称不能一样");
        }
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("parent_id",entity.getParentId());
        queryWrapper.eq("filename",context.getNewFilename());
        int count = count(queryWrapper);
        if (count>0){
            throw new DataStoragePlatformBusinessException("当前文件夹下面已经存在该文件名称");
        }
        context.setEntity(entity);
    }

}





