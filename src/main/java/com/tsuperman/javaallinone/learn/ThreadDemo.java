package com.tsuperman.javaallinone.learn;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.*;
import java.util.stream.IntStream;

public class ThreadDemo {

    // region 可重入锁
    public class ReentrantExample {
        public synchronized void outer() {
            System.out.println("outer");
            inner(); // 可重入调用
        }

        public synchronized void inner() {
            System.out.println("inner");
        }
    }

    // endregion

    // region Object监视器方法
    public class ProducerConsumerDemo {
        public void main(String[] args) {
            Buffer buffer = new Buffer();

            // 生产者线程
            Thread producer = new Thread(() -> {
                int i = 1;
                try {
                    while (true) {
                        buffer.put(i);
                        System.out.println("生产者生产了：" + i);
                        i++;
                        Thread.sleep(500); // 模拟生产过程
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            // 消费者线程
            Thread consumer = new Thread(() -> {
                try {
                    while (true) {
                        int value = buffer.get();
                        System.out.println("消费者消费了：" + value);
                        Thread.sleep(1000); // 模拟消费过程
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            // 启动线程
            producer.start();
            consumer.start();
        }
    }

    // 共享缓冲区类（只存一个 int 数据）
    class Buffer {
        private int data;
        private boolean hasData = false;

        public synchronized void put(int value) throws InterruptedException {
            while (hasData) {
                wait(); // 等待消费者消费
            }
            data = value;
            hasData = true;
            notify(); // 通知消费者可以消费了
        }

        public synchronized int get() throws InterruptedException {
            while (!hasData) {
                wait(); // 等待生产者生产
            }
            hasData = false;
            notify(); // 通知生产者可以继续生产了
            return data;
        }
    }

    // endregion

    // region Lock\trylock\Interrupt
    public class LockExample {
        private final Lock lock = new ReentrantLock();

        public void safeMethod() throws InterruptedException {
            lock.lock(); // 显式加锁
            try {
                System.out.println("执行安全代码");
            } finally {
                lock.unlock(); // 显式解锁，必须写在 finally！
            }

            if (lock.tryLock(2, TimeUnit.SECONDS)) {
                try {
                    System.out.println("2秒内获取到锁");
                } finally {
                    lock.unlock();
                }
            } else {
                System.out.println("2秒内未获取到锁");
            }

            // Interrupt
            try {
                lock.lockInterruptibly(); // 如果线程被中断，这里会抛异常
                try {
                    System.out.println("获取锁成功");
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                System.out.println("线程被中断，未获取到锁");
            }
        }
    }
    // endregion

    // region Condition

    public class  ConditionExample{

        private int data;
        private boolean hasData = false;
        private final Lock lock = new ReentrantLock();
        private final Condition notEmpty = lock.newCondition();
        private final Condition notFull = lock.newCondition();

        public void put(int value) throws InterruptedException {
            lock.lock();
            try {
                while (hasData) {
                    notFull.await(); // 等待 notFull 条件
                }
                data = value;
                hasData = true;
                System.out.println("生产：" + value);
                notEmpty.signal(); // 通知消费者
            } finally {
                lock.unlock();
            }
        }

        public int get() throws InterruptedException {
            lock.lock();
            try {
                while (!hasData) {
                    notEmpty.await(); // 等待 notEmpty 条件
                }
                hasData = false;
                System.out.println("消费：" + data);
                notFull.signal(); // 通知生产者
                return data;
            } finally {
                lock.unlock();
            }
        }
    }

    // endregion

    // region ReadWriteLock
    public class ReadWriteExample {
        private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
        private final Lock readLock = rwLock.readLock();
        private final Lock writeLock = rwLock.writeLock();
        private int value = 0;

        public int read() {
            readLock.lock();
            try {
                return value;
            } finally {
                readLock.unlock();
            }
        }

        public void write(int newValue) {
            writeLock.lock();
            try {
                value = newValue;
            } finally {
                writeLock.unlock();
            }
        }
    }

    // endregion

    // region StampedLock
    public class StampedLockExample {
        private final StampedLock stampedLock = new StampedLock();
        private int value = 100;

        public int read() {
            long stamp = stampedLock.tryOptimisticRead(); // 乐观读
            int temp = value;
            if (!stampedLock.validate(stamp)) {
                // 数据被写线程修改，退回到悲观读
                stamp = stampedLock.readLock();
                try {
                    temp = value;
                } finally {
                    stampedLock.unlockRead(stamp);
                }
            }
            return temp;
        }

        public void write(int newValue) {
            long stamp = stampedLock.writeLock();
            try {
                value = newValue;
            } finally {
                stampedLock.unlockWrite(stamp);
            }
        }
    }
    // endregion

    // region LockSupport：底层线程阻塞工具
    public class LockSupportExample {
        static Thread t1;

        public static void main(String[] args) throws InterruptedException {
            t1 = new Thread(() -> {
                System.out.println("线程1 正在等待...");
                LockSupport.park();
                System.out.println("线程1 被唤醒");
            });
            t1.start();

            Thread.sleep(1000); // 等待t1先进入park
            LockSupport.unpark(t1); // 唤醒t1,允许unpark在park之前调用
        }
    }
    // endregion

    // region Semaphore：信号量控制资源访问
    public class SemaphoreExample {
        private static final Semaphore semaphore = new Semaphore(3); // 最多3个许可，构造函数中指定公平性，默认非公平

        public static void main(String[] args) {
            for (int i = 1; i <= 10; i++) {
                final int num = i;
                new Thread(() -> {
                    try {
                        semaphore.acquire(); // 获取许可
                        System.out.println("线程" + num + " 获得许可");
                        Thread.sleep(1000); // 模拟任务执行
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        System.out.println("线程" + num + " 释放许可");
                        semaphore.release(); // 释放许可
                    }
                }).start();
            }
        }
    }
    // endregion

    // region CountDownLatch：一次性闭锁
    public class CountDownLatchExample {
        private static final CountDownLatch latch = new CountDownLatch(5);

        public static void main(String[] args) throws InterruptedException {
            for (int i = 1; i <= 5; i++) {
                final int num = i;
                new Thread(() -> {
                    System.out.println("任务" + num + " 执行中");
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {}
                    System.out.println("任务" + num + " 完成");
                    latch.countDown(); // 计数减一
                }).start();
            }

            System.out.println("等待所有任务完成...");
            latch.await(); // 阻塞直到计数为0
            System.out.println("所有任务已完成，主线程继续");
        }
    }
    // endregion

    // region CyclicBarrier：可重用栅栏
    public class CyclicBarrierExample {
        private static final CyclicBarrier barrier = new CyclicBarrier(3, () -> {
            System.out.println(">>>> 一组任务准备完毕，统一出发");
        });

        public static void main(String[] args) {
            for (int i = 1; i <= 6; i++) {
                final int num = i;
                new Thread(() -> {
                    System.out.println("线程" + num + " 准备好");
                    try {
                        barrier.await(); // 等待其他线程
                        System.out.println("线程" + num + " 执行任务");
                    } catch (Exception e) {}
                }).start();
            }
        }
    }
    // endregion

    // region Phaser：动态阶段同步器（CyclicBarrier 升级版）
    public class PhaserFullExample {

        public static void main(String[] args) throws InterruptedException {
            Phaser phaser = new Phaser(0); // 初始参与者为 0，后面动态注册

            // 启动3个初始线程
            for (int i = 1; i <= 3; i++) {
                int id = i;
                Thread t = new Thread(() -> runTask(phaser, id, true));
                phaser.register(); // 动态注册参与者
                t.start();
            }

            Thread.sleep(1000);

            // 动态增加一个线程（只执行前两个阶段）
            Thread newThread = new Thread(() -> runTask(phaser, 99, false));
            phaser.register(); // 注册新线程
            newThread.start();
        }

        private static void runTask(Phaser phaser, int id, boolean allPhases) {
            log(id, "注册阶段数：" + phaser.getPhase());

            // 阶段1：加载数据
            log(id, "开始加载数据");
            sleep(500);
            log(id, "加载完成");
            phaser.arriveAndAwaitAdvance();

            // 阶段2：处理数据
            log(id, "开始处理数据");
            sleep(500);
            log(id, "处理完成");
            phaser.arriveAndAwaitAdvance();

            // 阶段3：是否继续执行
            if (!allPhases) {
                log(id, "完成前两阶段，退出 phaser");
                phaser.arriveAndDeregister(); // 注销参与者
                return;
            }

            // 阶段3：保存数据
            log(id, "开始保存数据");
            sleep(500);
            log(id, "保存完成");
            phaser.arriveAndAwaitAdvance();

            log(id, "所有阶段完成，线程结束");
        }

        private static void log(int id, String msg) {
            System.out.printf("[线程 %02d] %s\n", id, msg);
        }

        private static void sleep(long ms) {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException ignored) {}
        }
    }
    // endregion

    // region 正确中断线程的标准做法
    public static class StandardInterruptTemplate implements Runnable {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // 阻塞操作
                    Thread.sleep(1000);
                    System.out.println("执行任务中...");
                } catch (InterruptedException e) {
                    System.out.println("被中断了，退出线程");
                    Thread.currentThread().interrupt(); // 重新设置中断标志
                    break;
                }
            }
        }

        public static void main(String[] args) throws InterruptedException {
            Thread t = new Thread(new StandardInterruptTemplate());
            t.start();
            Thread.sleep(3000);
            t.interrupt();
        }
    }
    // endregion

    // region 线程池拒绝策略
    public class ThreadPoolExecutorExample {
        public static void main(String[] args) {
            ThreadPoolExecutor executor = new ThreadPoolExecutor(
                    2, 4,
                    60, TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(2),
                    Executors.defaultThreadFactory(),
                    new ThreadPoolExecutor.AbortPolicy()
            );

            for (int i = 1; i <= 6; i++) {
                final int taskId = i;
                executor.execute(() -> {
                    System.out.println("任务 " + taskId + " 执行线程：" + Thread.currentThread().getName());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {}
                });
            }

            executor.shutdown();
        }
    }
    // endregion

    // region ExecutorService —— 通用线程池接口
    public class ExecutorServiceExample {
        public static void main(String[] args) throws Exception {
            // 此处仅做演示，不建议这样创建线程池
            ExecutorService executor = Executors.newFixedThreadPool(2);
            executor.execute(() -> System.out.println("Runnable任务执行"));
            Future<String> future = executor.submit(() -> {
                Thread.sleep(500);
                return "Callable任务返回值";
            });
            System.out.println("等待任务结果...");
            System.out.println("返回值：" + future.get());
            executor.shutdown();
        }
    }
    // endregion

    // region ScheduledExecutorService —— 定时/周期任务线程池
    public class ScheduledExecutorExample {
        public static void main(String[] args) {
            ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(
                    1,
                    Executors.defaultThreadFactory(),
                    new ThreadPoolExecutor.AbortPolicy()
            );

            scheduler.schedule(() -> {
                System.out.println("2秒后执行一次性任务");
            }, 2, TimeUnit.SECONDS);

            scheduler.scheduleAtFixedRate(() -> {
                System.out.println("周期任务执行时间：" + System.currentTimeMillis());
            }, 0, 1, TimeUnit.SECONDS);

            Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                System.out.println("关闭调度器");
                scheduler.shutdown();
            }, 5, TimeUnit.SECONDS);
        }
    }
    // endregion

    // region ForkJoinPool —— 分治并行计算线程池（Java 7+）
    public class ForkJoinPoolExample {

        //  RecursiveTask 是 ForkJoin 任务的基类（有返回值）
        static class SumTask extends RecursiveTask<Integer> {
            private final int start, end;

            SumTask(int start, int end) {
                this.start = start;
                this.end = end;
            }

            @Override
            protected Integer compute() {
                //  如果任务范围小于等于10，直接计算
                if (end - start <= 10) {
                    return IntStream.rangeClosed(start, end).sum();
                }

                //  否则拆分任务，递归处理
                int mid = (start + end) / 2;

                SumTask left = new SumTask(start, mid);      // 左边任务
                SumTask right = new SumTask(mid + 1, end);   // 右边任务

                left.fork(); //  异步启动 left 子任务（交给线程池）
                int rightResult = right.compute(); //  当前线程执行 right
                int leftResult = left.join();      //  等待 left 子任务执行完

                return leftResult + rightResult;   // 合并结果
            }
        }

        public static void main(String[] args) {
            ForkJoinPool pool = new ForkJoinPool();
            SumTask task = new SumTask(1, 100); //  创建任务：求 1~100 的和

            int result = pool.invoke(task); //  提交任务并等待最终结果

            System.out.println("1 到 100 的总和是：" + result);
        }
    }
    // endregion

    // region WorkStealingPool（慎用） —— 工作窃取线程池（Java 8+）
    public class WorkStealingPoolExample {
        public static void main(String[] args) throws InterruptedException {
            ExecutorService pool = Executors.newWorkStealingPool(); // 默认CPU核心数
            // 提交10个并发任务
            IntStream.rangeClosed(1, 10).forEach(i ->
                    pool.submit(() -> {
                        System.out.println("任务" + i + " 执行线程：" + Thread.currentThread().getName());
                        try {
                            Thread.sleep(500); // 模拟计算任务
                        } catch (InterruptedException ignored) {}
                    })
            );

            Thread.sleep(2000); // 防止主线程过早结束
        }
    }
    // endregion

    // region CompletionService —— 异步任务结果收集器
    public class CompletionServiceExample {
        public static void main(String[] args) throws Exception {
            ExecutorService pool = Executors.newFixedThreadPool(3);
            CompletionService<String> cs = new ExecutorCompletionService<>(pool);

            for (int i = 1; i <= 5; i++) {
                final int id = i;
                cs.submit(() -> {
                    Thread.sleep((6 - id) * 300); // 模拟不同耗时
                    return "任务 " + id + " 完成";
                });
            }

            // 谁先完成就谁先拿结果
            for (int i = 1; i <= 5; i++) {
                String result = cs.take().get();
                System.out.println("收到结果：" + result);
            }

            pool.shutdown();
        }
    }
    // endregion

    // region AtomicInteger —— 原子整型计数器
    public class AtomicIntegerExample {
        private static final AtomicInteger counter = new AtomicInteger(0);

        public static void main(String[] args) throws InterruptedException {
            Runnable task = () -> {
                for (int i = 0; i < 1000; i++) {
                    counter.incrementAndGet(); // 原子 +1
                }
            };

            Thread t1 = new Thread(task);
            Thread t2 = new Thread(task);
            t1.start();
            t2.start();

            t1.join();
            t2.join();

            System.out.println("最终计数值：" + counter.get()); // 应该是 2000
        }
    }
    // endregion

    // region AtomicReference —— 原子对象引用更新
    public class AtomicReferenceExample {
        static class Person {
            String name;

            Person(String name) {
                this.name = name;
            }

            public String toString() {
                return "Person{name='" + name + "'}";
            }
        }

        public static void main(String[] args) {
            AtomicReference<Person> ref = new AtomicReference<>(new Person("Alice"));

            System.out.println("原始对象：" + ref.get());

            Person oldPerson = ref.get();
            Person newPerson = new Person("Bob");

            boolean updated = ref.compareAndSet(oldPerson, newPerson);
            System.out.println("更新成功？" + updated);
            System.out.println("当前对象：" + ref.get());
        }
    }
    // endregion

    // region Future / FutureTask
    public class FutureExample {
        public static void main(String[] args) throws Exception {
            ExecutorService executor = Executors.newSingleThreadExecutor();

            Callable<String> callableTask = () -> {
                Thread.sleep(1000);
                return "任务完成";
            };

            Future<String> future = executor.submit(callableTask);

            System.out.println("等待结果...");
            String result = future.get();  // 阻塞直到任务完成
            System.out.println("任务结果：" + result);

            executor.shutdown();
        }
    }
    // endregion

    // region Exchanger
    public class ExchangerExample {
        public static void main(String[] args) {
            Exchanger<String> exchanger = new Exchanger<>();

            Thread t1 = new Thread(() -> {
                try {
                    String data = "数据A";
                    System.out.println("线程1准备交换：" + data);
                    String received = exchanger.exchange(data);
                    System.out.println("线程1收到：" + received);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            Thread t2 = new Thread(() -> {
                try {
                    String data = "数据B";
                    System.out.println("线程2准备交换：" + data);
                    String received = exchanger.exchange(data);
                    System.out.println("线程2收到：" + received);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            t1.start();
            t2.start();
        }
    }
    // endregion

    // region BlockingQueue（阻塞队列体系）
    public class ProducerConsumerExample {
        public static void main(String[] args) {
            BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(5);

            // 生产者
            Runnable producer = () -> {
                int count = 0;
                try {
                    while (true) {
                        System.out.println("生产者生产：" + count);
                        queue.put(count++); // 阻塞等待空间
                        Thread.sleep(300);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            };

            // 消费者
            Runnable consumer = () -> {
                try {
                    while (true) {
                        int item = queue.take(); // 阻塞等待数据
                        System.out.println("消费者消费：" + item);
                        Thread.sleep(500);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            };

            new Thread(producer).start();
            new Thread(consumer).start();
        }
    }
    // endregion

    // region volatile 关键字
    public class VolatileVisibility {
        volatile static boolean running = true;

        public static void main(String[] args) throws InterruptedException {
            new Thread(() -> {
                while (running) {
                    // 如果不加 volatile，可能永远无法感知变化
                }
                System.out.println("线程退出");
            }).start();

            Thread.sleep(1000);
            running = false; // 修改会被 volatile 可见
            System.out.println("修改 running=false");
        }
    }

    public class VolatileNonAtomic {
        volatile static int count = 0;
        // AtomicInteger count = new AtomicInteger();

        public static void main(String[] args) throws InterruptedException {
            Runnable task = () -> {
                for (int i = 0; i < 1000; i++) {
                    count++; // 非原子性：读取、修改、写回

                    //count.incrementAndGet(); // 原子加
                }
            };

            Thread t1 = new Thread(task);
            Thread t2 = new Thread(task);
            t1.start(); t2.start();
            t1.join(); t2.join();

            System.out.println("最终 count：" + count); // 可能小于 2000
        }
    }


    // endregion

    // region final 的语义增强
    // 错误示例（❌ 非线程安全）：
    class UnsafeHolder {
        int a; // 非 final

        public UnsafeHolder() {
            a = 1; // 写入字段
            SomeGlobal.ref = this; // 发布 this，重排序可能导致 a=0
        }
    }
    // 正确示例：
    class SafeHolder {
        final int a;

        public SafeHolder() {
            a = 1;
            SomeGlobal.ref = this; // 禁止重排序，a 保证可见性
        }
    }
    class  SomeGlobal{
        public static Object ref;
    }
    // endregion

    // region ConcurrentHashMap
    public class ConcurrentHashMapExample {
        public static void main(String[] args) {
            ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

            // 原子插入
            map.put("a", 1);
            map.putIfAbsent("b", 2);

            // compute 原子更新
            map.compute("a", (key, val) -> val == null ? 0 : val + 10);

            // forEach 并发安全遍历
            map.forEach(1, (k, v) -> {
                System.out.println(k + " = " + v);
            });

            System.out.println("总元素数：" + map.size());
        }
    }
    // endregion

    // region CopyOnWriteArrayList / CopyOnWriteArraySet
    public class COWExample {
        public static void main(String[] args) throws InterruptedException {
            CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();

            // 写线程
            Thread writer = new Thread(() -> {
                for (int i = 0; i < 5; i++) {
                    list.add("item-" + i);
                    try { Thread.sleep(100); } catch (Exception ignored) {}
                }
            });

            // 读线程
            Thread reader = new Thread(() -> {
                while (true) {
                    System.out.println("当前列表：" + list);
                    try { Thread.sleep(150); } catch (Exception ignored) {}
                    if (list.size() == 5) break;
                }
            });

            writer.start();
            reader.start();
            writer.join();
            reader.join();
        }
    }
    // endregion
}
