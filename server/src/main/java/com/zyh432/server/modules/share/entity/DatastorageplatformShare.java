package com.zyh432.server.modules.share.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户分享表
 * @TableName datastorageplatform_share
 */
@TableName(value ="datastorageplatform_share")
@Data
public class DatastorageplatformShare implements Serializable {
    /**
     * 分享id
     */
    @TableId(value = "share_id")
    private Long share_id;

    /**
     * 分享名称
     */
    @TableField(value = "share_name")
    private String share_name;

    /**
     * 分享类型（0 有提取码）
     */
    @TableField(value = "share_type")
    private Integer share_type;

    /**
     * 分享类型（0 永久有效；1 7天有效；2 30天有效）
     */
    @TableField(value = "share_day_type")
    private Integer share_day_type;

    /**
     * 分享有效天数（永久有效为0）
     */
    @TableField(value = "share_day")
    private Integer share_day;

    /**
     * 分享结束时间
     */
    @TableField(value = "share_end_time")
    private Date share_end_time;

    /**
     * 分享链接地址
     */
    @TableField(value = "share_url")
    private String share_url;

    /**
     * 分享提取码
     */
    @TableField(value = "share_code")
    private String share_code;

    /**
     * 分享状态（0 正常；1 有文件被删除）
     */
    @TableField(value = "share_status")
    private Integer share_status;

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