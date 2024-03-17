package com.zyh432.server.modules.share.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.zyh432.core.constants.DataStoragePlatformConstants;
import com.zyh432.core.exception.DataStoragePlatformBusinessException;
import com.zyh432.core.response.ResponseCode;
import com.zyh432.core.utils.IdUtil;
import com.zyh432.core.utils.JwtUtil;
import com.zyh432.core.utils.UUIDUtil;
import com.zyh432.server.common.config.storagePlatformServerConfig;
import com.zyh432.server.modules.file.context.CopyFileContext;
import com.zyh432.server.modules.file.context.FileDownloadContext;
import com.zyh432.server.modules.file.context.QueryFileListContext;
import com.zyh432.server.modules.file.entity.DatastorageplatformUserFile;
import com.zyh432.server.modules.file.enums.DelFlagEnum;
import com.zyh432.server.modules.file.service.IUserFileService;
import com.zyh432.server.modules.file.vo.DataStoragePlatformUserFileVO;
import com.zyh432.server.modules.share.constants.ShareConstants;
import com.zyh432.server.modules.share.context.*;
import com.zyh432.server.modules.share.entity.DatastorageplatformShare;
import com.zyh432.server.modules.share.enums.ShareDayTypeEnum;
import com.zyh432.server.modules.share.enums.ShareStatusEnum;
import com.zyh432.server.modules.share.service.IShareFileService;
import com.zyh432.server.modules.share.service.IShareService;
import com.zyh432.server.modules.share.mapper.DatastorageplatformShareMapper;
import com.zyh432.server.modules.share.vo.*;
import com.zyh432.server.modules.user.entity.DatastorageplatformUser;
import com.zyh432.server.modules.user.service.IUserService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
* @author 790585941
* @description 针对表【datastorageplatform_share(用户分享表)】的数据库操作Service实现
* @createDate 2024-01-08 22:28:02
*/
@Service
public class ShareServiceImpl extends ServiceImpl<DatastorageplatformShareMapper, DatastorageplatformShare>
    implements IShareService {
    @Autowired
    private storagePlatformServerConfig config;

    @Autowired
    private IShareFileService iShareFileService;

    @Autowired
    private IUserFileService iUserFileService;

    @Autowired
    private IUserService iUserService;

    /**
     * 创建分享链接
     * <p>
     * 1、拼装分享实体，保存到数据库
     * 2、保存分享和对应文件的关联关系
     * 3、拼装返回实体并返回
     *
     * @param context
     * @return
     */
    @Transactional(rollbackFor = DataStoragePlatformBusinessException.class)
    @Override
    public DataStoragePlatformShareUrlVO create(CreateShareUrlContext context) {
        saveShare(context);
        saveShareFiles(context);
        return assembleShareVO(context);
    }


    /**
     * 查询用户的分享列表
     *
     * @param context
     * @return
     */
    @Override
    public List<DataStoragePlatformShareUrlListVO> getShares(QueryShareListContext context) {
        return baseMapper.selectShareVOListByUserId(context.getUserId());
    }

    /**
     * 取消分享链接
     * <p>
     * 1、校验用户操作权限
     * 2、删除对应的分享记录
     * 3、删除对应的分享文件关联关系记录
     *
     * @param context
     */
    @Transactional(rollbackFor = DataStoragePlatformBusinessException.class)
    @Override
    public void cancelShare(CancelShareContext context) {
        checkUserCancelSharePermission(context);
        doCancelShare(context);
        doCancelShareFiles(context);
    }



    /**
     * 校验分享码
     * <p>
     * 1、检查分享的状态是不是正常
     * 2、校验分享的分享码是不是正确
     * 3、生成一个短时间的分享token 返回给上游
     *
     * @param context
     * @return
     */
    @Override
    public String checkShareCode(CheckShareCodeContext context) {
        DatastorageplatformShare record = checkShareStatus(context.getShareId());
        context.setRecord(record);
        doCheckShareCode(context);
        return generateShareToken(context);
    }


    /**
     * 查询分享的详情
     * <p>
     * 1、校验分享的状态
     * 2、初始化分享实体
     * 3、查询分享的主体信息
     * 4、查询分享的文件列表
     * 5、查询分享者的信息
     *
     * @param context
     * @return
     */
    @Override
    public ShareDetailVO detail(QueryShareDetailContext context) {
        DatastorageplatformShare record = checkShareStatus(context.getShareId());
        context.setRecord(record);
        initShareVO(context);
        assembleMainShareInfo(context);
        assembleShareFilesInfo(context);
        assembleShareUserInfo(context);
        return context.getVo();
    }

    /**
     * 查询分享的简单详情
     * <p>
     * 1、校验分享的状态
     * 2、初始化分享实体
     * 3、查询分享的主体信息
     * 4、查询分享者的信息
     *
     * @param context
     * @return
     */
    @Override
    public ShareSimpleDetailVO simpleDetail(QueryShareSimpleDetailContext context) {
        DatastorageplatformShare record = checkShareStatus(context.getShareId());
        context.setRecord(record);
        initShareSimpleVO(context);
        assembleMainShareSimpleInfo(context);
        assembleShareSimpleUserInfo(context);
        return context.getVo();
    }

    /**
     * 获取下一级的文件列表
     * <p>
     * 1、校验分享的状态
     * 2、校验文件的ID实在分享的文件列表中
     * 3、查询对应文件的子文件列表，返回
     *
     * @param context
     * @return
     */
    @Override
    public List<DataStoragePlatformUserFileVO> fileList(QueryChildFileListContext context) {
        DatastorageplatformShare record = checkShareStatus(context.getShareId());
        context.setRecord(record);
        List<DataStoragePlatformUserFileVO> allUserFileRecords = checkFileIdIsOnShareStatusAndGetAllShareUserFiles(context.getShareId(), Lists.newArrayList(context.getParentId()));
        Map<Long, List<DataStoragePlatformUserFileVO>> parentIdFileListMap = allUserFileRecords.stream().collect(Collectors.groupingBy(DataStoragePlatformUserFileVO::getParentId));
        List<DataStoragePlatformUserFileVO> dataStoragePlatformUserFileVOS = parentIdFileListMap.get(context.getParentId());
        if (CollectionUtils.isEmpty(dataStoragePlatformUserFileVOS)) {
            return Lists.newArrayList();
        }
        return dataStoragePlatformUserFileVOS;
    }

    /**
     * 转存至我的网盘
     * <p>
     * 1、校验分享状态
     * 2、校验文件ID是否合法
     * 3、执行保存我的网盘动作
     *
     * @param context
     */
    @Override
    public void saveFiles(ShareSaveContext context) {
        checkShareStatus(context.getShareId());
        checkFileIdIsOnShareStatus(context.getShareId(), context.getFileIdList());
        doSaveFiles(context);
    }

    /**
     * 分享的文件下载
     * <p>
     * 1、校验分享状态
     * 2、校验文件ID的合法性
     * 3、执行文件下载的动作
     *
     * @param context
     */
    @Override
    public void download(ShareFileDownloadContext context) {
        checkShareStatus(context.getShareId());
        checkFileIdIsOnShareStatus(context.getShareId(), Lists.newArrayList(context.getFileId()));
        doDownload(context);
    }


    /*******************************************private*******************************************/

    /**
     * 执行分享文件下载的动作
     * 委托文件模块去做
     *
     * @param context
     */
    private void doDownload(ShareFileDownloadContext context) {
        FileDownloadContext fileDownloadContext = new FileDownloadContext();
        fileDownloadContext.setFileId(context.getFileId());
        fileDownloadContext.setUserId(context.getUserId());
        fileDownloadContext.setResponse(context.getResponse());
        iUserFileService.downloadWithoutCheckUser(fileDownloadContext);
    }

    /**
     * 执行保存我的网盘动作
     * 委托文件模块做文件拷贝的操作
     *
     * @param context
     */
    private void doSaveFiles(ShareSaveContext context) {
        CopyFileContext copyFileContext = new CopyFileContext();
        copyFileContext.setFileIdList(context.getFileIdList());
        copyFileContext.setTargetParentId(context.getTargetParentId());
        copyFileContext.setUserId(context.getUserId());
        iUserFileService.copy(copyFileContext);
    }


    /**
     * 校验文件ID是否属于某一个分享
     *
     * @param shareId
     * @param fileIdList
     */
    private void checkFileIdIsOnShareStatus(Long shareId, List<Long> fileIdList) {
        checkFileIdIsOnShareStatusAndGetAllShareUserFiles(shareId, fileIdList);
    }


    /**
     * 校验文件是否处于分享状态，返回该分享的所有文件列表
     *
     * @param shareId
     * @param fileIdList
     * @return
     */
    private List<DataStoragePlatformUserFileVO> checkFileIdIsOnShareStatusAndGetAllShareUserFiles(Long shareId, List<Long> fileIdList) {
        List<Long> shareFileIdList = getShareFileIdList(shareId);
        if (CollectionUtils.isEmpty(shareFileIdList)) {
            return Lists.newArrayList();
        }
        List<DatastorageplatformUserFile> allFileRecords = iUserFileService.findAllFileRecordsByFileIdList(shareFileIdList);
        if (CollectionUtils.isEmpty(allFileRecords)) {
            return Lists.newArrayList();
        }
        allFileRecords = allFileRecords.stream()
                .filter(Objects::nonNull)
                .filter(record -> Objects.equals(record.getDelFlag(), DelFlagEnum.NO.getCode()))
                .collect(Collectors.toList());

        List<Long> allFileIdList = allFileRecords.stream().map(DatastorageplatformUserFile::getFileId).collect(Collectors.toList());

        if (allFileIdList.containsAll(fileIdList)) {
            return iUserFileService.transferVOList(allFileRecords);
        }

        throw new DataStoragePlatformBusinessException(ResponseCode.SHARE_FILE_MISS);
    }


    /**
     * 拼装简单文件分享详情的用户信息
     *
     * @param context
     */
    private void assembleShareSimpleUserInfo(QueryShareSimpleDetailContext context) {
        DatastorageplatformUser record = iUserService.getById(context.getRecord().getCreateUser());
        if (Objects.isNull(record)) {
            throw new DataStoragePlatformBusinessException("用户信息查询失败");
        }
        ShareUserInfoVO shareUserInfoVO = new ShareUserInfoVO();

        shareUserInfoVO.setUserId(record.getUserId());
        shareUserInfoVO.setUsername(encryptUsername(record.getUsername()));

        context.getVo().setShareUserInfoVO(shareUserInfoVO);
    }

    /**
     * 填充简单分享详情实体信息
     *
     * @param context
     */
    private void assembleMainShareSimpleInfo(QueryShareSimpleDetailContext context) {
        DatastorageplatformShare record = context.getRecord();
        ShareSimpleDetailVO vo = context.getVo();
        vo.setShareId(record.getShareId());
        vo.setShareName(record.getShareName());
    }

    /**
     * 初始化简单分享详情的VO对象
     *
     * @param context
     */
    private void initShareSimpleVO(QueryShareSimpleDetailContext context) {
        ShareSimpleDetailVO vo = new ShareSimpleDetailVO();
        context.setVo(vo);
    }


    /**
     * 查询分享者的信息
     *
     * @param context
     */
    private void assembleShareUserInfo(QueryShareDetailContext context) {
        DatastorageplatformUser record = iUserService.getById(context.getRecord().getCreateUser());
        if (Objects.isNull(record)) {
            throw new DataStoragePlatformBusinessException("用户信息查询失败");
        }
        ShareUserInfoVO shareUserInfoVO = new ShareUserInfoVO();

        shareUserInfoVO.setUserId(record.getUserId());
        shareUserInfoVO.setUsername(encryptUsername(record.getUsername()));

        context.getVo().setShareUserInfoVO(shareUserInfoVO);
    }

    /**
     * 加密用户名称
     *
     * @param username
     * @return
     */
    private String encryptUsername(String username) {
        StringBuffer stringBuffer = new StringBuffer(username);
        stringBuffer.replace(DataStoragePlatformConstants.TWO_INT, username.length() - DataStoragePlatformConstants.TWO_INT, DataStoragePlatformConstants.COMMON_ENCRYPT_STR);
        return stringBuffer.toString();
    }

    /**
     * 查询分享对应的文件列表
     * <p>
     * 1、查询分享对应的文件ID集合
     * 2、根据文件ID来查询文件列表信息
     *
     * @param context
     */
    private void assembleShareFilesInfo(QueryShareDetailContext context) {
        List<Long> fileIdList = getShareFileIdList(context.getShareId());

        QueryFileListContext queryFileListContext = new QueryFileListContext();
        queryFileListContext.setUserId(context.getRecord().getCreateUser());
        queryFileListContext.setDelFlag(DelFlagEnum.NO.getCode());
        queryFileListContext.setFileIdList(fileIdList);

        List<DataStoragePlatformUserFileVO> dataStoragePlatformUserFileVOList = iUserFileService.getFileList(queryFileListContext);
        context.getVo().setDataStoragePlatformUserFileVOList(dataStoragePlatformUserFileVOList);
    }

    /**
     * 查询分享对应的文件ID集合
     *
     * @param shareId
     * @return
     */
    private List<Long> getShareFileIdList(Long shareId) {
        if (Objects.isNull(shareId)) {
            return Lists.newArrayList();
        }
        QueryWrapper queryWrapper = Wrappers.query();
        queryWrapper.select("file_id");
        queryWrapper.eq("share_id", shareId);
        List<Long> fileIdList = iShareFileService.listObjs(queryWrapper, value -> (Long) value);
        return fileIdList;
    }

    /**
     * 查询分享的主体信息
     *
     * @param context
     */
    private void assembleMainShareInfo(QueryShareDetailContext context) {
        DatastorageplatformShare record = context.getRecord();
        ShareDetailVO vo = context.getVo();
        vo.setShareId(record.getShareId());
        vo.setShareName(record.getShareName());
        vo.setCreateTime(record.getCreateTime());
        vo.setShareDay(record.getShareDay());
        vo.setShareEndTime(record.getShareEndTime());
    }

    /**
     * 初始化文件详情的VO实体
     *
     * @param context
     */
    private void initShareVO(QueryShareDetailContext context) {
        ShareDetailVO vo = new ShareDetailVO();
        context.setVo(vo);
    }


    /**
     * 生成一个短期的分享token
     *
     * @param context
     * @return
     */
    private String generateShareToken(CheckShareCodeContext context) {
        DatastorageplatformShare record = context.getRecord();
        String token = JwtUtil.generateToken(UUIDUtil.getUUID(), ShareConstants.SHARE_ID, record.getShareId(), ShareConstants.ONE_HOUR_LONG);
        return token;
    }

    /**
     * 校验分享码是不是正确
     *
     * @param context
     */
    private void doCheckShareCode(CheckShareCodeContext context) {
        DatastorageplatformShare record = context.getRecord();
        if (!Objects.equals(context.getShareCode(), record.getShareCode())) {
            throw new DataStoragePlatformBusinessException("分享码错误");
        }
    }


    /**
     * 检查分享的状态是不是正常
     *
     * @param shareId
     * @return
     */
    private DatastorageplatformShare checkShareStatus(Long shareId) {
        DatastorageplatformShare record = getById(shareId);

        if (Objects.isNull(record)) {
            throw new DataStoragePlatformBusinessException(ResponseCode.SHARE_CANCELLED);
        }

        if (Objects.equals(ShareStatusEnum.FILE_DELETED.getCode(), record.getShareStatus())) {
            throw new DataStoragePlatformBusinessException(ResponseCode.SHARE_FILE_MISS);
        }

        if (Objects.equals(ShareDayTypeEnum.PERMANENT_VALIDITY.getCode(), record.getShareDayType())) {
            return record;
        }

        if (record.getShareEndTime().before(new Date())) {
            throw new DataStoragePlatformBusinessException(ResponseCode.SHARE_EXPIRE);
        }

        return record;
    }


    /**
     * 取消文件和分享的关联关系数据
     *
     * @param context
     */
    private void doCancelShareFiles(CancelShareContext context) {
        QueryWrapper queryWrapper = Wrappers.query();
        queryWrapper.in("share_id", context.getShareIdList());
        queryWrapper.eq("create_user", context.getUserId());
        if (!iShareFileService.remove(queryWrapper)) {
            throw new DataStoragePlatformBusinessException("取消分享失败");
        }
    }

    /**
     * 执行取消文件分享的动作
     *
     * @param context
     */
    private void doCancelShare(CancelShareContext context) {
        List<Long> shareIdList = context.getShareIdList();
        if (!removeByIds(shareIdList)) {
            throw new DataStoragePlatformBusinessException("取消分享失败");
        }
    }


    /**
     * 检查用户是否拥有取消对应分享链接的权限
     *
     * @param context
     */
    private void checkUserCancelSharePermission(CancelShareContext context) {
        List<Long> shareIdList = context.getShareIdList();
        Long userId = context.getUserId();
        List<DatastorageplatformShare> records = listByIds(shareIdList);
        if (CollectionUtils.isEmpty(records)) {
            throw new DataStoragePlatformBusinessException("您无权限操作取消分享的动作");
        }
        for (DatastorageplatformShare record : records) {
            if (!Objects.equals(userId, record.getCreateUser())) {
                throw new DataStoragePlatformBusinessException("您无权限操作取消分享的动作");
            }
        }
    }


    /**
     * 拼装分享的实体，并保存到数据库中
     *
     * @param context
     */
    private void saveShare(CreateShareUrlContext context) {
        DatastorageplatformShare record = new DatastorageplatformShare();

        record.setShareId(IdUtil.get());
        record.setShareName(context.getShareName());
        record.setShareType(context.getShareType());
        record.setShareDayType(context.getShareDayType());

        Integer shareDay = ShareDayTypeEnum.getShareDayByCode(context.getShareDayType());
        if (Objects.equals(DataStoragePlatformConstants.MINUS_ONE_INT, shareDay)) {
            throw new DataStoragePlatformBusinessException("分享天数非法");
        }

        record.setShareDay(shareDay);
        record.setShareEndTime(DateUtil.offsetDay(new Date(), shareDay));
        record.setShareUrl(createShareUrl(record.getShareId()));
        record.setShareCode(createShareCode());
        record.setShareStatus(ShareStatusEnum.NORMAL.getCode());
        record.setCreateUser(context.getUserId());
        record.setCreateTime(new Date());

        if (!save(record)) {
            throw new DataStoragePlatformBusinessException("保存分享信息失败");
        }

        context.setRecord(record);
    }


    /**
     * 创建分享的URL
     *
     * @param shareId
     * @return
     */
    private String createShareUrl(Long shareId) {
        if (Objects.isNull(shareId)) {
            throw new DataStoragePlatformBusinessException("分享的ID不能为空");
        }
        String sharePrefix = config.getSharePrefix();
        if (sharePrefix.lastIndexOf(DataStoragePlatformConstants.SLASH_STR) == DataStoragePlatformConstants.MINUS_ONE_INT.intValue()) {
            sharePrefix += DataStoragePlatformConstants.SLASH_STR;
        }
        return sharePrefix + shareId;
    }

    /**
     * 创建分享的分享码
     *
     * @return
     */
    private String createShareCode() {
        return RandomStringUtils.randomAlphabetic(4).toLowerCase();
    }

    /**
     * 保存分享和分享文件的关联关系
     *
     * @param context
     */
    private void saveShareFiles(CreateShareUrlContext context) {
        SaveShareFilesContext saveShareFilesContext = new SaveShareFilesContext();
        saveShareFilesContext.setShareId(context.getRecord().getShareId());
        saveShareFilesContext.setShareFileIdList(context.getShareFileIdList());
        saveShareFilesContext.setUserId(context.getUserId());
        iShareFileService.saveShareFiles(saveShareFilesContext);
    }


    /**
     * 拼装对应的返回VO
     *
     * @param context
     * @return
     */
    private DataStoragePlatformShareUrlVO assembleShareVO(CreateShareUrlContext context) {
        DatastorageplatformShare record = context.getRecord();
        DataStoragePlatformShareUrlVO vo = new DataStoragePlatformShareUrlVO();
        vo.setShareId(record.getShareId());
        vo.setShareName(record.getShareName());
        vo.setShareUrl(record.getShareUrl());
        vo.setShareCode(record.getShareCode());
        vo.setShareStatus(record.getShareStatus());
        return vo;
    }
}




