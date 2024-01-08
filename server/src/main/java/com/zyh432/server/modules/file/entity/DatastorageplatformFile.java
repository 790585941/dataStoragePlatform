package com.zyh432.server.modules.file.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 物理文件信息表
 * @TableName datastorageplatform_file
 */
@TableName(value ="datastorageplatform_file")
@Data
public class DatastorageplatformFile implements Serializable {
    /**
     * 文件id
     */
    @TableId(value = "file_id")
    private Long file_id;

    /**
     * 文件名称
     */
    @TableField(value = "filename")
    private String filename;

    /**
     * 文件物理路径
     */
    @TableField(value = "real_path")
    private String real_path;

    /**
     * 文件实际大小
     */
    @TableField(value = "file_size")
    private String file_size;

    /**
     * 文件大小展示字符
     */
    @TableField(value = "file_size_desc")
    private String file_size_desc;

    /**
     * 文件后缀
     */
    @TableField(value = "file_suffix")
    private String file_suffix;

    /**
     * 文件预览的响应头Content-Type的值
     */
    @TableField(value = "file_preview_content_type")
    private String file_preview_content_type;

    /**
     * 文件唯一标识
     */
    @TableField(value = "identifier")
    private String identifier;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}