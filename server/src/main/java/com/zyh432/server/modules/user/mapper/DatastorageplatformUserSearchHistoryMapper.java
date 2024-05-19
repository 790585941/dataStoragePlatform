package com.zyh432.server.modules.user.mapper;

import com.zyh432.server.modules.user.context.QueryUserSearchHistoryContext;
import com.zyh432.server.modules.user.entity.DatastorageplatformUserSearchHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zyh432.server.modules.user.vo.UserSearchHistoryVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 790585941
* @description 针对表【datastorageplatform_user_search_history(用户搜索历史表)】的数据库操作Mapper
* @createDate 2024-01-08 22:22:00
* @Entity com.zyh432.server.modules.user.entity.DatastorageplatformUserSearchHistory
*/
public interface DatastorageplatformUserSearchHistoryMapper extends BaseMapper<DatastorageplatformUserSearchHistory> {

    List<UserSearchHistoryVo> selectUserSearchHistories(@Param("param") QueryUserSearchHistoryContext context);
}




