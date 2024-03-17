package com.zyh432.server.modules.share.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.zyh432.server.modules.file.vo.DataStoragePlatformUserFileVO;
import com.zyh432.web.serializer.Date2StringSerializer;
import com.zyh432.web.serializer.IdEncryptSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@ApiModel("分享详情的返回实体对象")
@Data
public class ShareDetailVO implements Serializable {

    private static final long serialVersionUID = -2446579294335071804L;

    @JsonSerialize(using = IdEncryptSerializer.class)
    @ApiModelProperty("分享的ID")
    private Long shareId;

    @ApiModelProperty("分享的名称")
    private String shareName;

    @ApiModelProperty("分享的创建时间")
    @JsonSerialize(using = Date2StringSerializer.class)
    private Date createTime;

    @ApiModelProperty("分享的过期类型")
    private Integer shareDay;

    @ApiModelProperty("分享的截止时间")
    @JsonSerialize(using = Date2StringSerializer.class)
    private Date shareEndTime;

    @ApiModelProperty("分享的文件列表")
    private List<DataStoragePlatformUserFileVO> dataStoragePlatformUserFileVOList;

    @ApiModelProperty("分享者的信息")
    private ShareUserInfoVO shareUserInfoVO;

}

