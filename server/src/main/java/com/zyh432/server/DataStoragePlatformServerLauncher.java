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
@RestController
@Api("测试接口类")
@EnableTransactionManagement
@MapperScan(basePackages = DataStoragePlatformConstants.BASE_COMPONENT_SCAN_PATH+ ".server.modules.**.mapper")
public class DataStoragePlatformServerLauncher {
    public static void main(String[] args) {
        SpringApplication.run(DataStoragePlatformServerLauncher.class);
    }

    @GetMapping("hello")
    public Data<String> hello(@NotBlank(message = "name不能为空") String name){
        System.out.println(Thread.currentThread().getContextClassLoader());
        return Data.success("hello "+name+"! have changed!");
    }
}
