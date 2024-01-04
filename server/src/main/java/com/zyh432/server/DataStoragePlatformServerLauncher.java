package com.zyh432.server;

import com.zyh432.core.constants.DataStoragePlatformConstants;
import com.zyh432.core.response.Data;
import io.swagger.annotations.Api;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = DataStoragePlatformConstants.BASE_COMPONENT_SCAN_PATH)
@ServletComponentScan(basePackages = DataStoragePlatformConstants.BASE_COMPONENT_SCAN_PATH)
@RestController
@Api("测试接口类")
public class DataStoragePlatformServerLauncher {
    public static void main(String[] args) {
        SpringApplication.run(DataStoragePlatformServerLauncher.class);
    }

    @GetMapping("hello")
    public Data<String> hello(String name){
        return Data.success("hello "+name+"!");
    }
}
