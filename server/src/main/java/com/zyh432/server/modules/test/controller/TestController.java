package com.zyh432.server.modules.test.controller;

import com.zyh432.core.response.Data;
import com.zyh432.server.common.annotation.LoginIgnore;
import com.zyh432.server.common.stream.channel.Channels;
import com.zyh432.stream.core.IStreamProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试处理器
 */
@RestController
public class TestController implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier(value = "defaultStreamProducer")
    private IStreamProducer producer;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


    /**
     * 测试流事件发布
     * @param name
     * @return
     */
    @GetMapping("stream/test")
    @LoginIgnore
    public Data streamTest(String name){
        com.zyh432.server.common.stream.event.TestEvent testEvent=new com.zyh432.server.common.stream.event.TestEvent();
        testEvent.setName(name);
        producer.sendMessage(Channels.TEST_OUTPUT,testEvent);
        return Data.success();
    }
}
