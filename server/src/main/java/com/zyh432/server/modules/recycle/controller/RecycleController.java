package com.zyh432.server.modules.recycle.controller;

import com.google.common.base.Splitter;
import com.zyh432.core.constants.DataStoragePlatformConstants;
import com.zyh432.core.response.Data;
import com.zyh432.core.utils.IdUtil;
import com.zyh432.server.common.utils.UserIdUtil;
import com.zyh432.server.modules.file.vo.DataStoragePlatformUserFileVO;
import com.zyh432.server.modules.recycle.context.DeleteContext;
import com.zyh432.server.modules.recycle.context.QueryRecycleFileListContext;
import com.zyh432.server.modules.recycle.context.RestoreContext;
import com.zyh432.server.modules.recycle.po.DeletePO;
import com.zyh432.server.modules.recycle.po.RestorePO;
import com.zyh432.server.modules.recycle.service.IRecycleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 回收站模块控制器
 */
@RestController
@Api(tags = "回收站模块")
@Validated
public class RecycleController {
    @Autowired
    private IRecycleService iRecycleService;

    @ApiOperation(
            value = "获取回收站文件列表",
            notes = "该接口提供了获取回收站文件列表的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("recycles")
    public Data<List<DataStoragePlatformUserFileVO>> recycles() {
        QueryRecycleFileListContext context = new QueryRecycleFileListContext();
        context.setUserId(UserIdUtil.get());
        List<DataStoragePlatformUserFileVO> result = iRecycleService.recycles(context);
        return Data.data(result);
    }


    @ApiOperation(
            value = "删除的文件批量还原",
            notes = "该接口提供了删除的文件批量还原的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PutMapping("recycle/restore")
    public Data restore(@Validated @RequestBody RestorePO restorePO) {
        RestoreContext context = new RestoreContext();
        context.setUserId(UserIdUtil.get());

        String fileIds = restorePO.getFileIds();
        List<Long> fileIdList = Splitter.on(DataStoragePlatformConstants.COMMON_SEPARATOR).splitToList(fileIds).stream().map(IdUtil::decrypt).collect(Collectors.toList());
        context.setFileIdList(fileIdList);

        iRecycleService.restore(context);
        return Data.success();
    }

    @ApiOperation(
            value = "删除的文件批量彻底删除",
            notes = "该接口提供了删除的文件批量彻底删除的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @DeleteMapping("recycle")
    public Data delete(@Validated @RequestBody DeletePO deletePO) {
        DeleteContext context = new DeleteContext();
        context.setUserId(UserIdUtil.get());

        String fileIds = deletePO.getFileIds();
        List<Long> fileIdList = Splitter.on(DataStoragePlatformConstants.COMMON_SEPARATOR).splitToList(fileIds).stream().map(IdUtil::decrypt).collect(Collectors.toList());
        context.setFileIdList(fileIdList);

        iRecycleService.delete(context);
        return Data.success();
    }
}
