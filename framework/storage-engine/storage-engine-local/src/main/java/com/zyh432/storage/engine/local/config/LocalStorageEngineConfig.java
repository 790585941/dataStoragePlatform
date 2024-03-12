package com.zyh432.storage.engine.local.config;

import com.zyh432.core.utils.FileUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "com.zyh432.storage.engine.local")
@Data
public class LocalStorageEngineConfig {
    /**
     * 实际存放路径的前缀
     */
    private String rootFilePath = FileUtil.generateDefaultStoreFileRealPath();

    /**
     * 实际存放文件分片的路径的前缀
     */
    private String rootFileChunkPath = FileUtil.generateDefaultStoreFileChunkRealPath();
}
