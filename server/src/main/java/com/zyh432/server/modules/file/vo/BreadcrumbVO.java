package com.zyh432.server.modules.file.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.zyh432.server.modules.file.entity.DatastorageplatformUserFile;
import com.zyh432.web.serializer.IdEncryptSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@ApiModel("面包屑列表展示实体")
@Data
public class BreadcrumbVO implements Serializable {

    private static final long serialVersionUID = -6113151935665730951L;

    @ApiModelProperty("文件ID")
    @JsonSerialize(using = IdEncryptSerializer.class)
    private Long id;

    @ApiModelProperty("父文件夹ID")
    @JsonSerialize(using = IdEncryptSerializer.class)
    private Long parentId;

    @ApiModelProperty("文件夹名称")
    private String name;

    /**
     * 实体转换
     *
     * @param record
     * @return
     */
    public static BreadcrumbVO transfer(DatastorageplatformUserFile record) {
        BreadcrumbVO vo = new BreadcrumbVO();

        if (Objects.nonNull(record)) {
            vo.setId(record.getFileId());
            vo.setParentId(record.getParentId());
            vo.setName(record.getFilename());
        }

        return vo;
    }

}