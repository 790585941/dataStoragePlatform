package com.zyh432.server.modules.log.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 错误日志表
 * @TableName datastorageplatform_error_log
 */
@TableName(value ="datastorageplatform_error_log")
@Data
public class DatastorageplatformErrorLog implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 日志内容
     */
    @TableField(value = "log_content")
    private String log_content;

    /**
     * 日志状态：0 未处理 1 已处理
     */
    @TableField(value = "log_status")
    private Integer log_status;

    /**
     * 创建人
     */
    @TableField(value = "create_user")
    private Long create_user;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date create_time;

    /**
     * 更新人
     */
    @TableField(value = "update_user")
    private Long update_user;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date update_time;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}