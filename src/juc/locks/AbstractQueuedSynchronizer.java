package juc.locks;

import sun.misc.Unsafe;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;

/**
 * 提供一个框架来实现依赖于先进先出（FIFO）等待队列的阻塞锁和相关的同步器（信号灯，事件等）。
 * <p>
 * 此类支持默认排他模式和共享模式之一或两者。
 * 当以独占方式进行获取时，其他线程尝试进行的获取将无法成功。
 * 由多个线程获取的共享模式可能（但不一定）成功。该类不“理解”这些差异，只是从机械意义上说，当共享模式获取成功时，下一个等待线程（如果存在）还必须确定它是否也可以获取。
 * 在不同模式下等待的线程共享相同的FIFO队列。
 *
 * @author hui.zhong
 * @date 2020/8/30
 */
public abstract class AbstractQueuedSynchronizer extends AbstractOwnableSynchronizer {

    protected AbstractQueuedSynchronizer() {
    }


    /**
     * 等待队列节点类。
     */
    static final class Node {

        /**
         * 指示节点正在共享模式下等待的标记
         */
        static final Node SHARED = new Node();

        /**
         * 指示节点正在独占模式下等待的标记
         */
        static final Node EXCLUSIVE = null;

        /**
         * waitStatus值，指示线程已取消
         */
        static final int CANCELLED = 1;

        /**
         * waitStatus值，指示后续线程需要释放
         */
        static final int SIGNAL = -1;

        /**
         * waitStatus值，指示线程正在等待条件
         */
        static final int CONDITION = -2;

        /**
         * waitStatus值，指示下一个acquireShared应该无条件传播
         */
        static final int PROPAGATE = -3;

        volatile int waitStatus;

        volatile Node prev;

        volatile Node next;

        volatile Thread thread;

        /**
         * 链接到等待条件的下一个节点，或者特殊的共享模式。
         * 因为条件队列仅当以独占模式持有时被访问，我们只需要一个简单的链接队列以在节点等待时保留节点条件。
         * 然后将它们转移到队列中重新获取。
         * 而且由于条件只能是排他性的，我们通过使用特殊值来表示共享字段来保存字段模式。
         */
        Node nextWaiter;

        /**
         * 如果节点在共享模式下等待，则返回true。
         *
         * @return
         */
        final boolean isShared() {
            return nextWaiter == SHARED;
        }

        /**
         * 返回上一个节点，如果为null，则抛出NullPointerException。
         * 当上一个节点不能为null时使用。
         * 空检查可能被删除，但可以帮助虚拟机。
         *
         * @return
         * @throws NullPointerException
         */
        final Node predecessor() throws NullPointerException {
            Node p = prev;
            if (p == null) {
                throw new NullPointerException();
            } else {
                return p;
            }
        }

        Node() {
        }

        Node(Thread thread, Node mode) {
            this.nextWaiter = mode;
            this.thread = thread;
        }

        Node(Thread thread, int waitStatus) {
            this.waitStatus = waitStatus;
            this.thread = thread;
        }
    }

    private volatile Node head;

    private volatile Node tail;

    private volatile int state;

    public int getState() {
        return state;
    }

    public void setState(int newState) {
        this.state = newState;
    }

    protected final boolean compareAndSetState(int expect, int update) {
        return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
    }

    /**
     * 将节点插入队列，必要时进行初始化。
     *
     * @param node
     * @return
     */
    private Node enq(final Node node) {
        for (; ; ) {
            Node t = tail;
            if (t == null) {
                // 初始化之后，下一次循环设置
                if (compareAndSetHead(new Node())) {
                    tail = head;
                }
            } else {
                node.prev = t;
                if (compareAndSetTail(t, node)) {
                    t.next = node;
                    return t;
                }
            }
        }
    }

    /**
     * 如果没有获取到锁，则入队
     *
     * @param mode
     * @return
     */
    private Node addWaiter(Node mode) {
        Node node = new Node(Thread.currentThread(), mode);

        Node pred = tail;
        if (pred != null) {
            node.prev = pred;
            if (compareAndSetTail(pred, node)) {
                pred.next = node;
                return node;
            }
        }
        enq(node);
        return node;
    }

    /**
     * 以排他的不间断模式获取已在队列中的线程。
     * 用于条件等待方法以及获取。
     *
     * @param node
     * @param arg
     * @return
     */
    final boolean acquireQueued(final Node node, int arg) {
        boolean failed = true;
        try {
            boolean interrupted = false;
            // 一直循环等待获取锁
            for (; ; ) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    // node已经给head了，help GC
                    p.next = null;
                    failed = false;
                    return interrupted;
                }
                if (shouldParkAfterFailedAcquire(p, node)
                        && parkAndCheckInterrupt()) {
                    interrupted = true;
                }
            }
        } finally {
            if (failed) {
                cancelAcquire(node);
            }
        }
    }

    public final void acquire(int arg) {
        // 阻塞获取锁失败则以独占模式入队，并死循环获取锁
        if (!tryAcquire(arg)
                && acquireQueued(addWaiter(Node.EXCLUSIVE), arg)) {
            selfInterrupt();
        }
    }

    static void selfInterrupt() {
        Thread.currentThread().interrupt();
    }

    protected abstract boolean parkAndCheckInterrupt();

    protected abstract boolean shouldParkAfterFailedAcquire(Node p, Node node);

    protected abstract void cancelAcquire(Node node);

    protected boolean tryAcquire(int arg) {
        throw new UnsupportedOperationException();
    }

    private void setHead(Node node) {
        head = node;
        node.thread = null;
        node.prev = null;
    }

    /**
     * 头尾相等且(头的下一个为空或不为当前)-->开头正在处理(head 为虚拟节点，不存放线程)
     *
     * @return
     */
    public final boolean hasQueuedPredecessors() {
        Node t = tail;
        Node h = head;
        Node s;
        return h != t && ((s = h.next) == null || s.thread != Thread.currentThread());
    }

    protected boolean tryRelease(int arg) {
        throw new UnsupportedOperationException();
    }

    public final boolean release(int arg) {
        if (tryRelease(arg)) {
            Node h = head;
            if (h != null
                    && h.waitStatus != 0) {
                unparkSuccessor(h);
            }
            return true;
        }
        return false;
    }

    private void unparkSuccessor(Node node) {
        int ws = node.waitStatus;
        if (ws < 0) {
            compareAndSetWaitStatus(node, ws, 0);
        }
        Node s = node.next;
        if (s == null || s.waitStatus > 0) {
            s = null;
            for (Node t = tail; t != null && t != node; t = t.prev) {
                if (t.waitStatus <= 0) {
                    s = t;
                }
            }
        }
        if (s != null) {
            LockSupport.unpark(s.thread);
        }
    }

    public class ConditionObject implements Condition {

        private Node firstWaiter;

        private Node lastWaiter;

        public ConditionObject() {
        }

        /**
         * 创建新的CONDITION状态节点，加入条件队列队尾，并返回该节点；
         *
         * @return
         */
        private Node addConditionWaiter() {
            Node t = lastWaiter;
            x
            if (t != null && t.waitStatus != Node.CONDITION) {
                unlinkCancelledWaiters();
                t = lastWaiter;
            }
            Node node = new Node(Thread.currentThread(), Node.CONDITION);
            if (t == null) {
                firstWaiter = node;
            } else {
                t.nextWaiter = node;
            }
            lastWaiter = node;
            return node;
        }

        @Override
        public void await() throws InterruptedException {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {

            }
        }

        @Override
        public void awaitUninterruptibly() {

        }

        @Override
        public long awaitNanos(long nanosTimeout) throws InterruptedException {
            return 0;
        }

        @Override
        public boolean await(long time, TimeUnit unit) throws InterruptedException {
            return false;
        }

        @Override
        public boolean awaitUntil(Date deadline) throws InterruptedException {
            return false;
        }

        @Override
        public void signal() {

        }

        @Override
        public void signalAll() {

        }

        /**
         * 清除掉条件队列中所有被取消的节点；
         */
        private void unlinkCancelledWaiters() {
            Node t = firstWaiter;
            Node trail = null;
            while (t != null) {
                Node next = t.nextWaiter;
                if (t.waitStatus != Node.CONDITION) {
                    t.nextWaiter = null;
                    if (trail == null) {
                        firstWaiter = next;
                    } else {
                        trail.nextWaiter = next;
                    }
                    if (next == null) {
                        lastWaiter = trail;
                    }
                } else {
                    trail = t;
                }
                t = next;
            }
        }
    }

    final int fullyRelease(Node node) {
        boolean failed = true;
        try {
            int savedState = getState();
            if (release(savedState)) {
                failed = false;
                return savedState;
            } else {
                throw new IllegalMonitorStateException();
            }
        } finally {
            if (failed) {
                node.waitStatus = Node.CANCELLED;
            }
        }
    }


    private final boolean compareAndSetHead(Node update) {
        // head 为null才可以设置
        return unsafe.compareAndSwapObject(this, headOffset, null, update);
    }

    private final boolean compareAndSetTail(Node expect, Node update) {
        return unsafe.compareAndSwapObject(this, tailOffset, expect, update);
    }

    private static final boolean compareAndSetWaitStatus(Node node, int expect, int update) {
        return unsafe.compareAndSwapInt(node, waitStatusOffset,
                expect, update);
    }

    /**
     * native unsafe
     */
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long stateOffset;
    private static final long headOffset;
    private static final long tailOffset;
    private static final long waitStatusOffset;
    private static final long nextOffset;

    static {
        try {
            stateOffset = unsafe.objectFieldOffset(AbstractQueuedSynchronizer.class.getDeclaredField("state"));
            headOffset = unsafe.objectFieldOffset(AbstractQueuedSynchronizer.class.getDeclaredField("head"));
            tailOffset = unsafe.objectFieldOffset(AbstractQueuedSynchronizer.class.getDeclaredField("tail"));
            waitStatusOffset = unsafe.objectFieldOffset(AbstractQueuedSynchronizer.class.getDeclaredField("waitStatus"));
            nextOffset = unsafe.objectFieldOffset(AbstractQueuedSynchronizer.class.getDeclaredField("next"));
        } catch (Exception exception) {
            throw new Error(exception);
        }
    }
}
