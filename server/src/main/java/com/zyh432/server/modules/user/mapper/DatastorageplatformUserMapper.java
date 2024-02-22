package com.zyh432.server.modules.user.mapper;

import com.zyh432.server.modules.user.entity.DatastorageplatformUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
* @author 790585941
* @description 针对表【datastorageplatform_user(用户信息表)】的数据库操作Mapper
* @createDate 2024-01-08 22:22:00
* @Entity com.zyh432.server.modules.user.entity.DatastorageplatformUser
*/
public interface DatastorageplatformUserMapper extends BaseMapper<DatastorageplatformUser> {

    /**
     * 通过用户名称查询用户设置的密保问题
     * @param username
     * @return
     */
    String selectQuestionByUsername(@Param("username") String username);
}




