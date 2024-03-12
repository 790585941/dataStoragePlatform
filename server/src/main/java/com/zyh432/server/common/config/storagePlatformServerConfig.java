package com.zyh432.server.common.config;

import com.zyh432.core.constants.DataStoragePlatformConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "com.zyh432.server")
@Data
public class storagePlatformServerConfig {
    /**
     * 文件分片的过期天数
     */
    private Integer chunkFileExpirationDays = DataStoragePlatformConstants.ONE_INT;
}
