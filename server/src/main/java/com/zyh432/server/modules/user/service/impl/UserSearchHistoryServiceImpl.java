package com.zyh432.server.modules.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyh432.server.modules.user.context.QueryUserSearchHistoryContext;
import com.zyh432.server.modules.user.entity.DatastorageplatformUserSearchHistory;
import com.zyh432.server.modules.user.service.IUserSearchHistoryService;
import com.zyh432.server.modules.user.mapper.DatastorageplatformUserSearchHistoryMapper;
import com.zyh432.server.modules.user.vo.UserSearchHistoryVo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 790585941
* @description 针对表【datastorageplatform_user_search_history(用户搜索历史表)】的数据库操作Service实现
* @createDate 2024-01-08 22:22:00
*/
@Service(value = "userSearchHistoryService")
public class UserSearchHistoryServiceImpl extends ServiceImpl<DatastorageplatformUserSearchHistoryMapper, DatastorageplatformUserSearchHistory>
    implements IUserSearchHistoryService {


    /**
     * 查询用户的搜索历史记录，默认十条
     * @param context
     * @return
     */
    @Override
    public List<UserSearchHistoryVo> getUserSearchHistories(QueryUserSearchHistoryContext context) {
        return baseMapper.selectUserSearchHistories(context);
    }
}




