package com.zyh432.server.modules.share.context;

import com.zyh432.server.modules.share.entity.DatastorageplatformShare;
import com.zyh432.server.modules.share.vo.ShareSimpleDetailVO;
import lombok.Data;

import java.io.Serializable;

/**
 * 查询分享简单详情上下文实体信息
 */
@Data
public class QueryShareSimpleDetailContext implements Serializable {

    /**
     * 分享的ID
     */
    private Long shareId;

    /**
     * 分享对应的实体信息
     */
    private DatastorageplatformShare record;

    /**
     * 简单分享详情的VO对象
     */
    private ShareSimpleDetailVO vo;

}
