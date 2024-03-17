package com.zyh432.server.modules.share.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.zyh432.web.serializer.IdEncryptSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "创建分享链接的返回实体对象")
@Data
public class DataStoragePlatformShareUrlVO {
    private static final long serialVersionUID = 3468789641541361147L;

    @JsonSerialize(using = IdEncryptSerializer.class)
    @ApiModelProperty("分享链接的ID")
    private Long shareId;

    @ApiModelProperty("分享链接的名称")
    private String shareName;

    @ApiModelProperty("分享链接的URL")
    private String shareUrl;

    @ApiModelProperty("分享链接的分享码")
    private String shareCode;

    @ApiModelProperty("分享链接的状态")
    private Integer shareStatus;
}
