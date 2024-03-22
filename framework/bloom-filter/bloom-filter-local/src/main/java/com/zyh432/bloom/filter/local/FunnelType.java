package com.zyh432.bloom.filter.local;

import com.google.common.hash.Funnel;
import com.google.common.hash.Funnels;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.charset.StandardCharsets;

/**
 * 数据类型通道枚举类
 */
@AllArgsConstructor
@Getter
public enum FunnelType {
    /**
     * 长整型的数据通道
     */
    LONG(Funnels.longFunnel()),

    /**
     * 整型的数据通道
     */
    INTEGER(Funnels.integerFunnel()),

    /**
     * 字符串的数据通道
     */
    STRING(Funnels.stringFunnel(StandardCharsets.UTF_8));

    /**
     * 数据通道
     */
    private  Funnel funnel;
}
