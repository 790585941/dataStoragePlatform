package com.zyh432.server.modules.share.service;

import com.zyh432.server.modules.file.vo.DataStoragePlatformUserFileVO;
import com.zyh432.server.modules.share.context.*;
import com.zyh432.server.modules.share.entity.DatastorageplatformShare;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zyh432.server.modules.share.vo.DataStoragePlatformShareUrlListVO;
import com.zyh432.server.modules.share.vo.DataStoragePlatformShareUrlVO;
import com.zyh432.server.modules.share.vo.ShareDetailVO;
import com.zyh432.server.modules.share.vo.ShareSimpleDetailVO;

import java.util.List;

/**
* @author 790585941
* @description 针对表【datastorageplatform_share(用户分享表)】的数据库操作Service
* @createDate 2024-01-08 22:28:02
*/
public interface IShareService extends IService<DatastorageplatformShare> {

    /**
     * 创建分享链接
     * @param context
     * @return
     */
    DataStoragePlatformShareUrlVO create(CreateShareUrlContext context);

    /**
     * 查询用户的分享列表
     * @param context
     * @return
     */
    List<DataStoragePlatformShareUrlListVO> getShares(QueryShareListContext context);

    /**
     * 取消分享链接
     * @param context
     */
    void cancelShare(CancelShareContext context);

    /**
     * 校验分享码
     * @param context
     * @return
     */
    String checkShareCode(CheckShareCodeContext context);

    /**
     * 查询分享的详情
     * @param context
     * @return
     */
    ShareDetailVO detail(QueryShareDetailContext context);

    /**
     * 查询分享的简单详情
     * @param context
     * @return
     */
    ShareSimpleDetailVO simpleDetail(QueryShareSimpleDetailContext context);

    /**
     * 获取下一级的文件列表
     * @param context
     * @return
     */
    List<DataStoragePlatformUserFileVO> fileList(QueryChildFileListContext context);

    /**
     * 转存至我的网盘
     * @param context
     */
    void saveFiles(ShareSaveContext context);

    /**
     * 分享的文件下载
     * @param context
     */
    void download(ShareFileDownloadContext context);
}
