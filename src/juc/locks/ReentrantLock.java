package juc.locks;

import java.util.concurrent.TimeUnit;

/**
 * @author hui.zhong
 * @date 2020/8/30
 */
public class ReentrantLock implements Lock {

    private final Sync sync;

    abstract static class Sync extends AbstractQueuedSynchronizer {

        abstract void lock();

        /**
         * 获取非公平锁
         *
         * @param acquires
         * @return
         */
        final boolean nonfairTryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            // aqs state 标示获取锁的次数
            int c = getState();
            if (c == 0) {
                if (compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            } else if (c != 0 && current == getExclusiveOwnerThread()) {
                int nectc = c + acquires;
                if (nectc < 0) {
                    throw new Error("Maximum lock count exceeded");
                }
                setState(nectc);
                return true;
            }
            return false;
        }

        protected final boolean tryRelease(int releases) {
            int c = getState() - releases;
            if (Thread.currentThread() != getExclusiveOwnerThread()) {
                throw new IllegalMonitorStateException();
            }
            boolean free = false;
            if (c == 0) {
                free = true;
                setExclusiveOwnerThread(null);
            }
            setState(c);
            return free;
        }

        protected final boolean isHeldExclusively() {
            return getExclusiveOwnerThread() == Thread.currentThread();
        }

        final ConditionObject newCondition() {
            return new ConditionObject();
        }

        final Thread getOwner() {
            return getState() == 0 ? null : getExclusiveOwnerThread();
        }

        final int getHoldCount() {
            return isHeldExclusively() ? getState() : 0;
        }

        final boolean isLocked() {
            return getState() != 0;
        }
    }

    static final class NonfairSync extends Sync {
        final void lock() {
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
            } else {
                // 阻塞并等待获取锁
                acquire(1);
            }
        }

        protected boolean tryAcquire(int acquires) {
            return nonfairTryAcquire(acquires);
        }
    }

    static final class FairSync extends Sync {
        final void lock() {
            // 阻塞并等待获取锁
            acquire(1);
        }

        protected final boolean tryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                // 查询是否有任何线程在等待获取比当前线程更长的时间。
                if (!hasQueuedPredecessors()
                        && compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            } else if (c != 0 && current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0) {
                    throw new Error("Maximum lock count exceeded");
                }
                setState(nextc);
                return true;
            }
            return false;
        }
    }

    /**
     * 默认是非公平锁
     */
    public ReentrantLock() {
        sync = new NonfairSync();
    }

    public ReentrantLock(boolean fair) {
        sync = fair ? new FairSync() : new NonfairSync();
    }

    @Override
    public void lock() {
        sync.lock();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }

    @Override
    public boolean tryLock() {
        return sync.nonfairTryAcquire(1);
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireNanos(1, unit.toNanos(timeout));
    }

    @Override
    public void unlock() {
        sync.release(1);
    }

    @Override
    public Condition newCondition() {
        return sync.newCondition();
    }

    public int getHoldCount() {
        return sync.getHoldCount();
    }

    public boolean isHeldByCurrentThread() {
        return sync.isHeldExclusively();
    }

    public boolean isLocked() {
        return sync.isLocked();
    }

    public final boolean isFair() {
        return sync instanceof FairSync;
    }

    protected Thread getOwner() {
        return sync.getOwner();
    }
}
