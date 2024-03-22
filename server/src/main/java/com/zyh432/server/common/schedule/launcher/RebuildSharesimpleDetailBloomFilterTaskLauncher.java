package com.zyh432.server.common.schedule.launcher;

import com.zyh432.schedule.ScheduleManager;
import com.zyh432.server.common.schedule.task.CleanExpireChunkFileTask;
import com.zyh432.server.common.schedule.task.RebuildShareSimpleDetailBloomFilterTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 定时重建简单分享详情布隆过滤器任务触发器
 */
@Slf4j
@Component
public class RebuildSharesimpleDetailBloomFilterTaskLauncher implements CommandLineRunner {
    //每天
    private final static String CRON = "1 0 0 * * ? ";
    //每5秒
//  private final static String CRON = "0/5 * * * * ? ";

    @Autowired
    private RebuildShareSimpleDetailBloomFilterTask task;

    @Autowired
    private ScheduleManager scheduleManager;

    @Override
    public void run(String... args) throws Exception {
        scheduleManager.startTask(task, CRON);
    }

}

