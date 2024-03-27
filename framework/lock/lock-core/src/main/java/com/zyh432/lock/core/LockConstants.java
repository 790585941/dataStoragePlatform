package com.zyh432.lock.core;

/**
 * 锁相关公用常量类
 */
public interface LockConstants {
    /**
     * 公用lock的名称
     */
    String DATASTORAGEPLATFORM_LOCK="datastorageplatform-lock;";
    /**
     * 公用lock的path
     * 主要针对zookeeper等节点型软件
     */
    String DATASTORAGEPLATFORM_LOCK_PATH="/datastorageplatform-lock";
}
