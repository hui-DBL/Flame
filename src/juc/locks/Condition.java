package juc.locks;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author hui.zhong
 * @date 2020/8/30
 */
public interface Condition {

    void await() throws InterruptedException;

    void awaitUninterruptibly();

    long awitNanos(long nanosTimeout) throws InterruptedException;

    boolean await(long time, TimeUnit unit) throws InterruptedException;

    boolean awaitUnitl(Date deadline) throws InterruptedException;

    void signal();

    void signalAll();
}
