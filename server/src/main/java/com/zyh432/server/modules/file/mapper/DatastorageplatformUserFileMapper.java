package com.zyh432.server.modules.file.mapper;

import com.zyh432.server.modules.file.context.FileSearchContext;
import com.zyh432.server.modules.file.context.QueryFileListContext;
import com.zyh432.server.modules.file.entity.DatastorageplatformUserFile;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zyh432.server.modules.file.vo.DataStoragePlatformUserFileVO;
import com.zyh432.server.modules.file.vo.FileSearchResultVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 790585941
* @description 针对表【datastorageplatform_user_file(用户文件信息表)】的数据库操作Mapper
* @createDate 2024-01-08 22:25:23
* @Entity com.zyh432.server.modules.file.entity.DatastorageplatformUserFile
*/
public interface DatastorageplatformUserFileMapper extends BaseMapper<DatastorageplatformUserFile> {

    /**
     * 查询用户的文件列表
     * @param context
     * @return
     */
    List<DataStoragePlatformUserFileVO> selectFileList(@Param("param") QueryFileListContext context);

    /**
     * 文件搜索
     *
     * @param context
     * @return
     */
    List<FileSearchResultVO> searchFile(@Param("param") FileSearchContext context);
}




