package com.zyh432.server.modules.user.controller;


import com.zyh432.core.response.Data;
import com.zyh432.core.utils.IdUtil;
import com.zyh432.server.common.annotation.LoginIgnore;
import com.zyh432.server.common.utils.UserIdUtil;
import com.zyh432.server.modules.user.context.QueryUserSearchHistoryContext;
import com.zyh432.server.modules.user.service.IUserSearchHistoryService;
import com.zyh432.server.modules.user.vo.UserSearchHistoryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;

import java.util.List;

@RestController
@Api(tags = "用户搜索历史")
public class UserSearchHistoryController {
    @Autowired
    private IUserSearchHistoryService iUserSearchHistoryService;

    @ApiOperation(
            value = "获取用户最新的搜索历史记录，默认十条",
            notes = "该接口提供了获取用户最新的搜索历史记录的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("user/search/histories")
    public Data<List<UserSearchHistoryVo>> getUserSearchHistories(){
        QueryUserSearchHistoryContext context=new QueryUserSearchHistoryContext();
        context.setUserId(UserIdUtil.get());
        List<UserSearchHistoryVo> result= iUserSearchHistoryService.getUserSearchHistories(context);
        return Data.data(result);
    }
}
