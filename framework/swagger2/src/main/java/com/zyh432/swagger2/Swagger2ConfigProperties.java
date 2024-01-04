package com.zyh432.swagger2;

import com.zyh432.core.constants.DataStoragePlatformConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


/**
 * swagger2配置属性实体
 */
@Data
@Component
@ConfigurationProperties(prefix = "swagger2")
public class Swagger2ConfigProperties {
    private boolean show = true;

    private String groupName = "dataStoragePlatform";

    private String basePackage = DataStoragePlatformConstants.BASE_COMPONENT_SCAN_PATH;

    private String title = "dataStoragePlatform-server";

    private String description = "dataStoragePlatform-server";

    private String termsOfServiceUrl = "http://127.0.0.1:${server.port}";

    private String contactName = "panghu";

    private String contactUrl = "https://blog.panghu.com";

    private String contactEmail = "790585941@qq.com";

    private String version = "1.0";
}
