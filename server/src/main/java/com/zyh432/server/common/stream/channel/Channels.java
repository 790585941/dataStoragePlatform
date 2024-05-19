package com.zyh432.server.common.stream.channel;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

/**
 * 事件通道定义类
 */
public interface Channels {
    String TEST_INPUT = "testInput";
    String TEST_OUTPUT = "testOutput";

    /**
     * 测试输入通道
     * @return
     */
    @Input(TEST_INPUT)
    SubscribableChannel testInput();

    /**
     * 测试输出通道
     * @return
     */
    @Output(TEST_OUTPUT)
    MessageChannel testOutput();
}
