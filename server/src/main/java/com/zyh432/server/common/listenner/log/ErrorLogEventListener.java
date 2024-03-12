package com.zyh432.server.common.listenner.log;

import com.zyh432.core.utils.IdUtil;
import com.zyh432.server.common.event.log.ErrorLogEvent;
import com.zyh432.server.modules.log.entity.DatastorageplatformErrorLog;
import com.zyh432.server.modules.log.service.IErrorLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 系统错误日志监听器
 */
@Component
public class ErrorLogEventListener {

    @Autowired
    private IErrorLogService iErrorLogService;

    /**
     * 监听系统错误日志事件，并保存到数据库中
     *
     * @param event
     */
    @EventListener(ErrorLogEvent.class)
    @Async(value = "eventListenerTaskExecutor")
    public void saveErrorLog(ErrorLogEvent event) {
        DatastorageplatformErrorLog record = new DatastorageplatformErrorLog();
        record.setId(IdUtil.get());
        record.setLogContent(event.getErrorMsg());
        record.setLogStatus(0);
        record.setCreateUser(event.getUserId());
        record.setCreateTime(new Date());
        record.setUpdateUser(event.getUserId());
        record.setUpdateTime(new Date());
        iErrorLogService.save(record);
    }

}
