package com.zyh432.server.modules.user.service;

import com.zyh432.server.modules.user.context.QueryUserSearchHistoryContext;
import com.zyh432.server.modules.user.entity.DatastorageplatformUserSearchHistory;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zyh432.server.modules.user.vo.UserSearchHistoryVo;

import java.util.List;

/**
* @author 790585941
* @description 针对表【datastorageplatform_user_search_history(用户搜索历史表)】的数据库操作Service
* @createDate 2024-01-08 22:22:00
*/
public interface IUserSearchHistoryService extends IService<DatastorageplatformUserSearchHistory> {

    /**
     * 查询用户的搜索历史记录，默认十条
     * @param context
     * @return
     */
    List<UserSearchHistoryVo> getUserSearchHistories(QueryUserSearchHistoryContext context);
}
