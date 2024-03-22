package com.zyh432.server.modules.share.mapper;

import com.zyh432.server.modules.share.entity.DatastorageplatformShare;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zyh432.server.modules.share.vo.DataStoragePlatformShareUrlListVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 790585941
* @description 针对表【datastorageplatform_share(用户分享表)】的数据库操作Mapper
* @createDate 2024-01-08 22:28:02
* @Entity com.zyh432.server.modules.share.entity.DatastorageplatformShare
*/
public interface DatastorageplatformShareMapper extends BaseMapper<DatastorageplatformShare> {

    /**
     * 查询用户的分享列表
     * @param userId
     * @return
     */
    List<DataStoragePlatformShareUrlListVO> selectShareVOListByUserId(@Param("userId") Long userId);

    /**
     * 滚动查询已存在的分享ID集合
     * @param startId
     * @param limit
     * @return
     */
    List<Long> rollingQueryShareId(@Param("startId")Long startId, @Param("limit")Long limit);
}




