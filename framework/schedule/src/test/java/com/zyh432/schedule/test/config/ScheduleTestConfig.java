package com.zyh432.schedule.test.config;

import com.zyh432.core.constants.DataStoragePlatformConstants;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * 单元测试配置类
 */
@SpringBootConfiguration
@ComponentScan(DataStoragePlatformConstants.BASE_COMPONENT_SCAN_PATH + ".schedule")
public class ScheduleTestConfig {
}

