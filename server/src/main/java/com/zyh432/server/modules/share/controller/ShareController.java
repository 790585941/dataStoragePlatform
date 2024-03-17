package com.zyh432.server.modules.share.controller;

import com.google.common.base.Splitter;
import com.zyh432.core.constants.DataStoragePlatformConstants;
import com.zyh432.core.response.Data;
import com.zyh432.core.utils.IdUtil;
import com.zyh432.server.common.annotation.LoginIgnore;
import com.zyh432.server.common.annotation.NeedShareCode;
import com.zyh432.server.common.utils.ShareIdUtil;
import com.zyh432.server.common.utils.UserIdUtil;
import com.zyh432.server.modules.file.vo.DataStoragePlatformUserFileVO;
import com.zyh432.server.modules.share.context.CreateShareUrlContext;
import com.zyh432.server.modules.share.converter.ShareConverter;
import com.zyh432.server.modules.share.po.CancelSharePO;
import com.zyh432.server.modules.share.po.CheckShareCodePO;
import com.zyh432.server.modules.share.po.CreateShareUrlPO;
import com.zyh432.server.modules.share.po.ShareSavePO;
import com.zyh432.server.modules.share.service.IShareService;
import com.zyh432.server.modules.share.vo.DataStoragePlatformShareUrlListVO;
import com.zyh432.server.modules.share.vo.DataStoragePlatformShareUrlVO;
import com.zyh432.server.modules.share.vo.ShareDetailVO;
import com.zyh432.server.modules.share.vo.ShareSimpleDetailVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.zyh432.server.modules.share.context.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "分享模块")
@RestController
@Validated
public class ShareController {
    @Autowired
    private IShareService iShareService;

    @Autowired
    private ShareConverter shareConverter;


    @ApiOperation(
            value = "创建分享链接",
            notes = "该接口提供了创建分享链接的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("share")
    public Data<DataStoragePlatformShareUrlVO> create(@Validated @RequestBody CreateShareUrlPO createShareUrlPO) {
        CreateShareUrlContext context = shareConverter.createShareUrlPO2CreateShareUrlContext(createShareUrlPO);

        String shareFileIds = createShareUrlPO.getShareFileIds();
        List<Long> shareFileIdList = Splitter.on(DataStoragePlatformConstants.COMMON_SEPARATOR).splitToList(shareFileIds).stream().map(IdUtil::decrypt).collect(Collectors.toList());

        context.setShareFileIdList(shareFileIdList);

        DataStoragePlatformShareUrlVO vo = iShareService.create(context);
        return Data.data(vo);
    }

    @ApiOperation(
            value = "查询分享链接列表",
            notes = "该接口提供了查询分享链接列表的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("shares")
    public Data<List<DataStoragePlatformShareUrlListVO>> getShares() {
        QueryShareListContext context = new QueryShareListContext();
        context.setUserId(UserIdUtil.get());
        List<DataStoragePlatformShareUrlListVO> result = iShareService.getShares(context);
        return Data.data(result);
    }

    @ApiOperation(
            value = "取消分享",
            notes = "该接口提供了取消分享的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @DeleteMapping("share")
    public Data cancelShare(@Validated @RequestBody CancelSharePO cancelSharePO) {
        CancelShareContext context = new CancelShareContext();

        context.setUserId(UserIdUtil.get());

        String shareIds = cancelSharePO.getShareIds();
        List<Long> shareIdList = Splitter.on(DataStoragePlatformConstants.COMMON_SEPARATOR).splitToList(shareIds).stream().map(IdUtil::decrypt).collect(Collectors.toList());
        context.setShareIdList(shareIdList);

        iShareService.cancelShare(context);
        return Data.success();
    }

    @ApiOperation(
            value = "校验分享码",
            notes = "该接口提供了校验分享码的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @LoginIgnore
    @PostMapping("share/code/check")
    public Data<String> checkShareCode(@Validated @RequestBody CheckShareCodePO checkShareCodePO) {
        CheckShareCodeContext context = new CheckShareCodeContext();

        context.setShareId(IdUtil.decrypt(checkShareCodePO.getShareId()));
        context.setShareCode(checkShareCodePO.getShareCode());

        String token = iShareService.checkShareCode(context);
        return Data.data(token);
    }

    @ApiOperation(
            value = "查询分享的详情",
            notes = "该接口提供了查询分享的详情的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @LoginIgnore
    @NeedShareCode
    @GetMapping("share")
    public Data<ShareDetailVO> detail() {
        QueryShareDetailContext context = new QueryShareDetailContext();
        context.setShareId(ShareIdUtil.get());
        ShareDetailVO vo = iShareService.detail(context);
        return Data.data(vo);
    }

    @ApiOperation(
            value = "查询分享的简单详情",
            notes = "该接口提供了查询分享的简单详情的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @LoginIgnore
    @GetMapping("share/simple")
    public Data<ShareSimpleDetailVO> simpleDetail(@NotBlank(message = "分享的ID不能为空") @RequestParam(value = "shareId", required = false) String shareId) {
        QueryShareSimpleDetailContext context = new QueryShareSimpleDetailContext();
        context.setShareId(IdUtil.decrypt(shareId));
        ShareSimpleDetailVO vo = iShareService.simpleDetail(context);
        return Data.data(vo);
    }

    @ApiOperation(
            value = "获取下一级文件列表",
            notes = "该接口提供了获取下一级文件列表的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("share/file/list")
    @NeedShareCode
    @LoginIgnore
    public Data<List<DataStoragePlatformUserFileVO>> fileList(@NotBlank(message = "文件的父ID不能为空") @RequestParam(value = "parentId", required = false) String parentId) {
        QueryChildFileListContext context = new QueryChildFileListContext();
        context.setShareId(ShareIdUtil.get());
        context.setParentId(IdUtil.decrypt(parentId));
        List<DataStoragePlatformUserFileVO> result = iShareService.fileList(context);
        return Data.data(result);
    }

    @ApiOperation(
            value = "保存至我的网盘",
            notes = "该接口提供了保存至我的网盘的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @NeedShareCode
    @PostMapping("share/save")
    public Data saveFiles(@Validated @RequestBody ShareSavePO shareSavePO) {
        ShareSaveContext context = new ShareSaveContext();

        String fileIds = shareSavePO.getFileIds();
        List<Long> fileIdList = Splitter.on(DataStoragePlatformConstants.COMMON_SEPARATOR).splitToList(fileIds).stream().map(IdUtil::decrypt).collect(Collectors.toList());
        context.setFileIdList(fileIdList);

        context.setTargetParentId(IdUtil.decrypt(shareSavePO.getTargetParentId()));
        context.setUserId(UserIdUtil.get());
        context.setShareId(ShareIdUtil.get());

        iShareService.saveFiles(context);
        return Data.success();
    }

    @ApiOperation(
            value = "分享文件下载",
            notes = "该接口提供了分享文件下载的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("share/file/download")
    @NeedShareCode
    public void download(@NotBlank(message = "文件ID不能为空") @RequestParam(value = "fileId", required = false) String fileId,
                         HttpServletResponse response) {
        ShareFileDownloadContext context = new ShareFileDownloadContext();
        context.setFileId(IdUtil.decrypt(fileId));
        context.setShareId(ShareIdUtil.get());
        context.setUserId(UserIdUtil.get());
        context.setResponse(response);
        iShareService.download(context);
    }

}
