package com.nowcoder.wenda;

import javafx.beans.binding.IntegerExpression;

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.context.transaction.TestTransaction.start;


class MyThread extends Thread{
    private int tid;
    public MyThread(int tid){
        this.tid = tid;
    }

    //继承Thread,重载run,tid代表线程索引,i是每次每个线程的打印次数
    @Override
    public void run() {
        try {
            for (int i = 0; i < 10; i++) {
                Thread.sleep(1000);
                System.out.println(String.format("%d : %d", tid, i));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
//class MyThread extends Thread {
//    private int tid;
//
//    public MyThread(int tid) {
//        this.tid = tid;
//    }
//
//    @Override
//    public void run() {
//        try {
//            for (int i = 0; i < 10; ++i) {
//                Thread.sleep(1000);
//                System.out.println(String.format("%d:%d", tid, i));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}

//消费者线程,一直取,异步两条消费线程依次去blockqueue中取
class Consumer implements Runnable{
    private  BlockingQueue<String> q;
    public Consumer(BlockingQueue<String> q){
        this.q = q;
    }
    @Override
    public void run() {
        try{
            while (true){
                System.out.println(Thread.currentThread().getName() + " : " + q.take());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

//class Consumer implements Runnable {
//    private BlockingQueue<String> q;
//    public Consumer(BlockingQueue<String> q) {
//        this.q = q;
//    }
//    @Override
//    public void run() {
//        try {
//            while (true) {
//                System.out.println(Thread.currentThread().getName() + ":" + q.take());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}

//生产者线程,往blockqueue中插入0~99
class Producer implements Runnable{
    private  BlockingQueue<String> q;
    public Producer(BlockingQueue<String> q){
        this.q = q;
    }
    @Override
    public void run() {
        try{
            for (int i = 0; i < 100; i++){
                Thread.sleep(100);
                q.put(String.valueOf(i));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
//class Producer implements Runnable {
//    private BlockingQueue<String> q;
//    public Producer(BlockingQueue<String> q) {
//        this.q = q;
//    }
//    @Override
//    public void run() {
//        try {
//            for (int i = 0; i < 100; ++i) {
//                Thread.sleep(1000);
//                q.put(String .valueOf(i));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}

//起始测试代码
public class MultiThreadTests {
    public static void testThread() {
        for (int i = 0; i < 10; ++i) {
           // new MyThread(i).start();
        }


        for (int i = 0; i < 10; ++i) {
            final int fin = i;  //作为线程索引
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int j= 0; j < 10; j++) {
                            Thread.sleep(1000);
                            System.out.println(String.format("T2 :: %d : %d", fin, j));  //j是时间间隔次数
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }
//        for (int i = 0; i < 10; ++i) {
//            final int finalI = i;
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        for (int j = 0; j < 10; ++j) {
//                            Thread.sleep(1000);
//                            System.out.println(String.format("T2 %d: %d:", finalI, j));
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
//        }
    }

    private static Object obj = new Object();

    public static void testSynchronized1() {
        //如果锁相同则,不能夹着,t3 5 后面是t4 7这种.必须,t3从0~9.再换下一个可能t3或者t4
        synchronized (obj) {
            try {
                for (int j = 0; j < 10; ++j) {
                    Thread.sleep(1000);
                    System.out.println(String.format("T3 %d", j));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void testSynchronized2() {
        synchronized (new Object()) {   //新的锁,就互相会夹杂.
            try {
                for (int j = 0; j < 10; ++j) {
                    Thread.sleep(1000);
                    System.out.println(String.format("T4 %d", j));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void testSynchronized() {
        for (int i = 0; i < 10; ++i) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    testSynchronized1();
                    testSynchronized2();
                }
            }).start();
        }
    }

    public static void testBlockingQueue() {
        BlockingQueue<String> q = new ArrayBlockingQueue<>(10);
        new Thread(new Producer(q)).start();   //里面的Producer实现了Runnable.
        new  Thread(new Consumer(q), "Consumer1").start();   //""是给线程起名字
        new  Thread(new Consumer(q), "Consumer2").start();
//        BlockingQueue<String> q = new ArrayBlockingQueue<String>(10);
//        new Thread(new Producer(q)).start();
//        new Thread(new Consumer(q), "Consumer1").start();
//        new Thread(new Consumer(q), "Consumer2").start();
    }

    //threadLocalUserIds是线程本地变量,每一个线程都有自己的一个副本.每个线程都不一样.就是0进去之后,直接写入.并且在该线程保存了副本.下一次1进去,那么是新的线程,在新的线程中保存副本,并不影响之前的上个副本值0.所以最后打印0~4都有.
    //userid每个线程都一样为最后的i,是因为只有一个线程值,不存在副本,0进去为0,1进去把0改为1,最后的userid就是最后线程更改的同一变量4.
    private static ThreadLocal<Integer> threadLocalUserIds = new ThreadLocal<>();
    private static int userId;
//    private static ThreadLocal<Integer> threadLocalUserIds = new ThreadLocal<>();  //线程独立变量
//    private static int userId;

    public static void testThreadLocal() {
        for (int i = 0; i < 10; ++i) {  //i为线程数
            final int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        threadLocalUserIds.set(finalI);    //先设置线程副本的值
                        Thread.sleep(1000);
                        System.out.println("ThreadLocal : " + threadLocalUserIds.get());
                    }catch (Exception e){
                        e.printStackTrace();
                    }
//                    try {
//                        threadLocalUserIds.set(finalI);
//                        Thread.sleep(1000);
//                        System.out.println("ThreadLocal:" + threadLocalUserIds.get());
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                }
            }).start();
        }

        for (int i = 0; i < 5; ++i) {
            final int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        userId = finalI;
                        Thread.sleep(1000);
                        System.out.println("UserId:" + userId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

    }

    //线程池是跟 继承Thread或者实现Runnable写在类上一样,都是不需要.start()
    public static void testExecutor() {
        //ExecutorService service = Executors.newSingleThreadExecutor();   //单线程的线程池,Executor1结束所有的打印任务,才会进行Executor2的打印任务
        ExecutorService service = Executors.newFixedThreadPool(2);   //两个线程的线程池,则下面的提交任务两个线程同时进行
        service.submit(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10; ++i) {
                    try {
                        Thread.sleep(1000);
                        System.out.println("Executor1:" + i);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        service.submit(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++){
                    try{
                        Thread.sleep(1000);
                        System.out.println("Executor2 : " + i);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
//        service.submit(new Runnable() {
//            @Override
//            public void run() {
//                for (int i = 0; i < 10; ++i) {
//                    try {
//                        Thread.sleep(1000);
//                        System.out.println("Executor2:" + i);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });

        service.shutdown();

        //轮询,每隔一秒查看如果没有完成提交的任务,则打印""
        while (!service.isTerminated()) {
            try {
                Thread.sleep(1000);
                System.out.println("Wait for termination.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static int counter = 0;
    private static AtomicInteger atomicInteger = new AtomicInteger(0);   //有自增一的方法incrementAndGet

//    private static int counter = 0;
//    private static AtomicInteger atomicInteger = new AtomicInteger(0);

    public static void testWithoutAtomic() {
        for (int i = 0; i < 10; i++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                        for (int j = 0; j < 1000; j++){
                            counter++;  //正常10个线程,每个线程的每1秒+1,最后应该返回100(数量越大越出错.但是非原子操作会有并发的错误
                            System.out.println(counter);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }
//        for (int i = 0; i < 10; ++i) {   //开了10条线程
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Thread.sleep(1000);
//                        for (int j = 0; j < 10; ++j) {
//                            counter++;
//                            System.out.println(counter);
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
//        }
    }
    public static void testWithAtomic() {
        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                        for (int j = 0; j < 100; j++) {
                            Thread.sleep(1000);   //同一个数,每一秒相当于一个线程+1个数,然后现在10个线程,一秒就原子变量+10.最后一定会出现1000.不一定是最后一个.
                            System.out.println(atomicInteger.incrementAndGet());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
//    public static void testWithAtomic() {
//        for (int i = 0; i < 10; ++i) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Thread.sleep(1000);
//                        for (int j = 0; j < 10; ++j) {
//                            System.out.println(atomicInteger.incrementAndGet());
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
//        }
//    }

    public static  void testAtomic() {
        //testWithoutAtomic();
        testWithAtomic();
    }

    //future有一个等待异步结果的概念,可以设定timeout,可以看异常
    public static void testFuture() {
        ExecutorService service = Executors.newSingleThreadExecutor();   //框架制造单线程的线程池
        Future<Integer> future = service.submit(new Callable<Integer>() {   //方法返回值,不过是异步的
            @Override
            public Integer call() throws Exception {
//                Thread.sleep(1000);  //隔一秒钟才返回值
//                return 1;   //返回给future
                throw new IllegalArgumentException("异常啊啊啊啊啊");
            }
        });

        try {
            //System.out.println(future.get());  //这里是一直在等待值的返回.形成异步
            System.out.println(future.get(100, TimeUnit.MILLISECONDS));  //100ms后如果没有返回值则报错
        }catch (Exception e){
            e.printStackTrace();   //可以打印出异常
        }
//        ExecutorService service = Executors.newSingleThreadExecutor();
//        Future<Integer> future = service.submit(new Callable<Integer>() {
//            @Override
//            public Integer call() throws Exception {
//                //Thread.sleep(1000);
//                throw new IllegalArgumentException("异常");
//                //return 1;
//            }
//        });
//
//        service.shutdown();
//        try {
//            System.out.println(future.get());
//            //System.out.println(future.get(100, TimeUnit.MILLISECONDS));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
    public static void main(String[] argv) {
        //testThread();
        //testSynchronized();
        //testBlockingQueue();
        //testThreadLocal();
        //testExecutor();
        //testAtomic();
        testFuture();
    }
}
