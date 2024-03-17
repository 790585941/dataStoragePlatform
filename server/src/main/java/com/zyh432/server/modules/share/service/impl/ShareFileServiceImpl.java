package com.zyh432.server.modules.share.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.zyh432.core.exception.DataStoragePlatformBusinessException;
import com.zyh432.core.utils.IdUtil;
import com.zyh432.server.modules.share.context.SaveShareFilesContext;
import com.zyh432.server.modules.share.entity.DatastorageplatformShareFile;
import com.zyh432.server.modules.share.service.IShareFileService;
import com.zyh432.server.modules.share.mapper.DatastorageplatformShareFileMapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
* @author 790585941
* @description 针对表【datastorageplatform_share_file(用户分享文件表)】的数据库操作Service实现
* @createDate 2024-01-08 22:28:02
*/
@Service
public class ShareFileServiceImpl extends ServiceImpl<DatastorageplatformShareFileMapper, DatastorageplatformShareFile>
    implements IShareFileService {
    /**
     * 保存分享的文件的对应关系
     *
     * @param context
     */
    @Override
    public void saveShareFiles(SaveShareFilesContext context) {
        Long shareId = context.getShareId();
        List<Long> shareFileIdList = context.getShareFileIdList();
        Long userId = context.getUserId();

        List<DatastorageplatformShareFile> records = Lists.newArrayList();

        for (Long shareFileId : shareFileIdList) {
            DatastorageplatformShareFile record = new DatastorageplatformShareFile();
            record.setId(IdUtil.get());
            record.setShareId(shareId);
            record.setFileId(shareFileId);
            record.setCreateUser(userId);
            record.setCreateTime(new Date());
            records.add(record);
        }

        if (!saveBatch(records)) {
            throw new DataStoragePlatformBusinessException("保存文件分享关联关系失败");
        }
    }

}




