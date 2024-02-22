package com.zyh432.server.modules.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyh432.core.constants.DataStoragePlatformConstants;
import com.zyh432.core.exception.DataStoragePlatformBusinessException;
import com.zyh432.core.utils.IdUtil;
import com.zyh432.server.modules.file.constants.FileConstants;
import com.zyh432.server.modules.file.context.CreateFolderContext;
import com.zyh432.server.modules.file.entity.DatastorageplatformUserFile;
import com.zyh432.server.modules.file.enums.DelFlagEnum;
import com.zyh432.server.modules.file.enums.FolderFlagEnum;
import com.zyh432.server.modules.file.service.IUserFileService;
import com.zyh432.server.modules.file.mapper.DatastorageplatformUserFileMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.xml.datatype.DatatypeConstants;
import java.util.Date;

/**
* @author 790585941
* @description 针对表【datastorageplatform_user_file(用户文件信息表)】的数据库操作Service实现
* @createDate 2024-01-08 22:25:23
*/
@Service(value = "userFileService")
public class UserFileServiceImpl extends ServiceImpl<DatastorageplatformUserFileMapper, DatastorageplatformUserFile>
    implements IUserFileService {

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

    /****************************************private **********************************/

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

}





