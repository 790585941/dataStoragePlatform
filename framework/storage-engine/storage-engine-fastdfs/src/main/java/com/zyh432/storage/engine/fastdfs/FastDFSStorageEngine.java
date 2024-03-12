package com.zyh432.storage.engine.fastdfs;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.zyh432.core.constants.DataStoragePlatformConstants;
import com.zyh432.core.exception.DataStoragePlatformFrameworkException;
import com.zyh432.core.utils.FileUtil;
import com.zyh432.storage.engine.core.AbstractStorageEngine;
import com.zyh432.storage.engine.core.context.*;
import com.zyh432.storage.engine.fastdfs.config.FastDFSStorageEngineConfig;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * 基于FastDFS实现的文件存储引擎
 */
@Component
public class FastDFSStorageEngine extends AbstractStorageEngine {
    @Autowired
    private FastFileStorageClient client;

    @Autowired
    private FastDFSStorageEngineConfig config;


    /**
     * 执行保存物理文件的动作
     * 下沉到具体的子类去实现
     *
     * @param context
     */
    @Override
    protected void doStore(StoreFileContext context) throws IOException {
        StorePath storePath = client.uploadFile(config.getGroup(), context.getInputStream(), context.getTotalSize(), FileUtil.getFileExtName(context.getFilename()));
        context.setRealPath(storePath.getFullPath());
    }

    /**
     * 执行删除物理文件的动作
     * 下沉到子类去实现
     *
     * @param context
     * @throws IOException
     */
    @Override
    protected void doDelete(DeleteFileContext context) throws IOException {
        List<String> realFilePathList = context.getRealFilePathList();
        if (CollectionUtils.isNotEmpty(realFilePathList)) {
            realFilePathList.stream().forEach(client::deleteFile);
        }
    }

    /**
     * 执行保存文件分片
     * 下沉到底层去实现
     *
     * @param context
     * @throws IOException
     */
    @Override
    protected void doStoreChunk(StoreFileChunkContext context) throws IOException {
        throw new DataStoragePlatformFrameworkException("FastDFS不支持分片上传的操作");
    }

    /**
     * 执行文件分片的动作
     * @param context
     */
    @Override
    protected void doMergeFile(MergeFileContext context)  throws IOException  {
        throw new DataStoragePlatformFrameworkException("FastDFS不支持分片上传的操作");
    }

    /**
     * 读取文件内容并写入到输出流中
     * 下沉到子类去实现
     *
     * @param context
     */
    @Override
    protected void doReadFile(ReadFileContext context) throws IOException {
        String realPath = context.getRealPath();
        //获取FastDFS的组名（group）和文件路径（path）
        String group = realPath.substring(DataStoragePlatformConstants.ZERO_INT, realPath.indexOf(DataStoragePlatformConstants.SLASH_STR));
        String path = realPath.substring(realPath.indexOf(DataStoragePlatformConstants.SLASH_STR) + DataStoragePlatformConstants.ONE_INT);

        DownloadByteArray downloadByteArray = new DownloadByteArray();
        byte[] bytes = client.downloadFile(group, path, downloadByteArray);
        //将从FastDFS下载的文件内容写入Servlet的输出流
        OutputStream outputStream = context.getOutputStream();
        outputStream.write(bytes);
        outputStream.flush();
        outputStream.close();
    }
}
