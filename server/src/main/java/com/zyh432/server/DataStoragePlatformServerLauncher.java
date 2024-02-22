package com.zyh432.server;

import com.zyh432.core.constants.DataStoragePlatformConstants;
import com.zyh432.core.response.Data;
import io.swagger.annotations.Api;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;

@SpringBootApplication(scanBasePackages = DataStoragePlatformConstants.BASE_COMPONENT_SCAN_PATH)
@ServletComponentScan(basePackages = DataStoragePlatformConstants.BASE_COMPONENT_SCAN_PATH)
@EnableTransactionManagement
@MapperScan(basePackages = DataStoragePlatformConstants.BASE_COMPONENT_SCAN_PATH+ ".server.modules.**.mapper")
public class DataStoragePlatformServerLauncher {
    public static void main(String[] args) {
        SpringApplication.run(DataStoragePlatformServerLauncher.class);
    }

}
