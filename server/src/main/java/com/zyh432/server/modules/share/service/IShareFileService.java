package com.zyh432.server.modules.share.service;

import com.zyh432.server.modules.share.context.SaveShareFilesContext;
import com.zyh432.server.modules.share.entity.DatastorageplatformShareFile;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 790585941
* @description 针对表【datastorageplatform_share_file(用户分享文件表)】的数据库操作Service
* @createDate 2024-01-08 22:28:02
*/
public interface IShareFileService extends IService<DatastorageplatformShareFile> {
    /**
     * 保存分享的文件的对应关系
     *
     * @param context
     */
    void saveShareFiles(SaveShareFilesContext context);
}
