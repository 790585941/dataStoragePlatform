package com.zyh432.storage.engine.local;

import com.zyh432.core.utils.FileUtil;
import com.zyh432.storage.engine.core.AbstractStorageEngine;
import com.zyh432.storage.engine.core.context.*;
import com.zyh432.storage.engine.local.config.LocalStorageEngineConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

/**
 * 本地文件存储引擎实现类
 */
@Component
public class LocalStorageEngine extends AbstractStorageEngine {
    @Autowired
    private LocalStorageEngineConfig config;

    @Override
    protected void doStore(StoreFileContext context) throws IOException {
        String basePath=config.getRootFilePath();
        String realFilePath=FileUtil.generateStoreFileRealPath(basePath, context.getFilename());
        FileUtil.writeStream2File(context.getInputStream(), new File(realFilePath), context.getTotalSize());
        context.setRealPath(realFilePath);
    }

    @Override
    protected void doDelete(DeleteFileContext context) throws IOException {
        FileUtil.deleteFiles(context.getRealFilePathList());
    }

    @Override
    protected void doStoreChunk(StoreFileChunkContext context) throws IOException {
        String basePath = config.getRootFileChunkPath();
        String realFilePath = FileUtil.generateStoreFileChunkRealPath(basePath, context.getIdentifier(), context.getChunkNumber());
        FileUtil.writeStream2File(context.getInputStream(), new File(realFilePath), context.getTotalSize());
        context.setRealPath(realFilePath);
    }

    /**
     * 执行文件分片的动作
     *
     * @param context
     */
    @Override
    protected void doMergeFile(MergeFileContext context) throws IOException{
        String basePath = config.getRootFilePath();
        String realFilePath = FileUtil.generateStoreFileRealPath(basePath, context.getFilename());
        FileUtil.createFile(new File(realFilePath));
        List<String> chunkPaths = context.getRealPathList();
        for (String chunkPath : chunkPaths) {
            FileUtil.appendWrite(Paths.get(realFilePath), new File(chunkPath).toPath());
        }
        FileUtil.deleteFiles(chunkPaths);
        context.setRealPath(realFilePath);
    }


    /**
     * 读取文件内容并写入到输出流中
     * 下沉到子类去实现
     *
     * @param context
     */
    @Override
    protected void doReadFile(ReadFileContext context) throws IOException {
        File file = new File(context.getRealPath());
        FileUtil.writeFile2OutputStream(new FileInputStream(file), context.getOutputStream(), file.length());

    }
}
