package com.zyh432.server.modules.file.service;

import com.zyh432.server.modules.file.context.CreateFolderContext;
import com.zyh432.server.modules.file.entity.DatastorageplatformUserFile;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 790585941
* @description 针对表【datastorageplatform_user_file(用户文件信息表)】的数据库操作Service
* @createDate 2024-01-08 22:25:23
*/
public interface IUserFileService extends IService<DatastorageplatformUserFile> {
    /**
     * 创建文件夹信息
     * @param userId
     * @return
     */
    Long createFolder(CreateFolderContext createFolderContext);

    /**
     * 查询用户的根文件夹信息
     * @param userId
     * @return
     */
    DatastorageplatformUserFile getUserRootFile(Long userId);
}
