package com.zyh432.server.modules.share.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户分享文件表
 * @TableName datastorageplatform_share_file
 */
@TableName(value ="datastorageplatform_share_file")
@Data
public class DatastorageplatformShareFile implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 分享id
     */
    @TableField(value = "share_id")
    private Long share_id;

    /**
     * 文件记录ID
     */
    @TableField(value = "file_id")
    private Long file_id;

    /**
     * 分享创建人
     */
    @TableField(value = "create_user")
    private Long create_user;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date create_time;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}