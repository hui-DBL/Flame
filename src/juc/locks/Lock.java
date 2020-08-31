package juc.locks;

import java.util.concurrent.TimeUnit;

/**
 * @author hui.zhong
 * @date 2020/8/30
 */
public interface Lock {

    void lock();

    void lockInterruptibly() throws InterruptedException;

    boolean tryLock();

    boolean tryLock(long time, TimeUnit unit) throws InterruptedException;

    void unlock();

    Condition newCondition();
}
