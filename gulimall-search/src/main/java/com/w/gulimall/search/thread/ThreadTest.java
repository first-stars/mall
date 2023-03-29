package com.w.gulimall.search.thread;

import java.util.concurrent.*;

/**
 * @author xin
 * @date 2023-02-06-11:09
 */
public class ThreadTest {

    public static ExecutorService service = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
//        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor();

//        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//            System.out.println("当前线程池" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("运行结果" + i);
//        }, service);

        CompletableFuture<Integer> integerCompletableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程池" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果" + i);
            return i;
        }, service);
        Integer integer = integerCompletableFuture.get();
        System.out.println(integer);
        System.out.println("main....end");

    }
}
