package juc.locks;

/**
 * 线程专有的同步器。
 * 此类提供了创建锁和相关的同步器（可能涉及所有权概念）的基础。
 * AbstractOwnableSynchronizer类本身并不管理或使用此信息。
 * 但是，子类和工具可以使用适当维护的值来帮助控制和监视访问并提供诊断。
 *
 * @author hui.zhong
 * @date 2020/8/30
 */
public abstract class AbstractOwnableSynchronizer {

    protected AbstractOwnableSynchronizer() {
    }

    /**
     * 独占模式同步的当前所有者。(拿到锁的线程？)
     */
    private Thread exclusiveOwnerThread;

    protected final void setExclusiveOwnerThread(Thread exclusiveOwnerThread) {
        this.exclusiveOwnerThread = exclusiveOwnerThread;
    }

    protected final Thread getExclusiveOwnerThread() {
        return exclusiveOwnerThread;
    }
}
