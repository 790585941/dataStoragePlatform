package com.zyh432.stream.core;

import com.google.common.collect.Maps;
import com.zyh432.core.exception.DataStoragePlatformFrameworkException;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 消息发送者顶级抽象父类
 */
public abstract class AbstractstreamProducer implements IStreamProducer{
    @Autowired
    private Map<String, MessageChannel> channelMap;

    /**
     * 发送消息
     * @param channelName
     * @param deploy
     * @return
     */
    @Override
    public boolean sendMessage(String channelName, Object deploy) {
        return sendMessage(channelName,deploy, Maps.newHashMap());
    }

    /**
     * 发送消息
     *
     * 1、参数校验
     * 2、执行发送前钩子函数
     * 3、执行发送的动作
     * 4、发送后置钩子函数
     * 5、返回结果
     * @param channelName
     * @param deploy
     * @param headers
     * @return
     */
    @Override
    public boolean sendMessage(String channelName, Object deploy, Map<String, Object> headers) {
        if(StringUtils.isBlank(channelName) || Objects.isNull(deploy)){
            throw  new DataStoragePlatformFrameworkException("the channelName or deploy can not be empty");
        }
        if (MapUtils.isEmpty(channelMap)){
            throw  new DataStoragePlatformFrameworkException("the channelMap can not be empty");
        }
        MessageChannel channel = channelMap.get(channelName);
        if (Objects.isNull(channel)){
            throw  new DataStoragePlatformFrameworkException("the channel name"+ channelName+" can not be found!");
        }
        Message<Object> message = MessageBuilder.createMessage(deploy, new MessageHeaders(headers));
        preSend(message);
        boolean result = channel.send(message);
        afterSend(message,result);
        return result;
    }

    /**
     * 发送消息的后置钩子函数
     * @param message
     * @param result
     */
    protected abstract void afterSend(Message<Object> message, boolean result);

    /**
     * 发送消息的前置钩子函数
     * @param message
     */
    protected abstract void preSend(Message<Object> message);
}
