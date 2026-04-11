package com.example.my_api_server.virtual_thread;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

@Slf4j
public class VTClass {
    static final int TASK_COUNT = 1000;
    static final Duration IO_DURATION = Duration.ofSeconds(1); //i/o작업 시간

    public static void main(String[] args) {
//        Thread tv1 = Thread.ofVirtual()
//                .name("가상스레드1")
//                .start(VTClass::IoRun);
        // 1. I/O 작업 비교
//        log.info("[i/o]플랫폼 스레드 시작!");
//        IoRun(Executors.newFixedThreadPool(200)); // 플랫폼 스레드 N개 생성(미리 생성)
//
//        log.info("[i/o]가상 스레드 시작!");
//        IoRun(Executors.newVirtualThreadPerTaskExecutor()); // 가상 스레드 필요한 개수만큼 생성(필요할때)
//
//        // 2. CPU 작업 비교
//        log.info("[cpu]플랫폼 스레드 시작!");
//        run(Executors.newFixedThreadPool(200)); // 플랫폼 스레드 N개 생성(미리 생성)
//        //2MB * 200 = 400MB만 고정해서 씁니다.
//        log.info("[cpu]가상 스레드 시작!");
//        run(Executors.newVirtualThreadPerTaskExecutor()); // 가상 스레드 필요한 개수만큼 생성(필요할때)
//        //1KB * TASKCOUNT = 5000kb = 5mb
//        log.info("[io]플랫폼 스레드 피닝 테스트 시작");
//        ioRunPinning(Executors.newFixedThreadPool(200));
//        log.info("[io]가상 스레드 피닝 테스트 시작");
//        ioRunPinning(Executors.newVirtualThreadPerTaskExecutor());

        log.info("[io]플랫폼 스레드 피닝 테스트2 시작");
        ioRunPinningRL(Executors.newFixedThreadPool(200));
        log.info("[io]가상 스레드 피닝 테스트2 시작");
        ioRunPinningRL(Executors.newVirtualThreadPerTaskExecutor());
    }

    // I/O 작업 측정 메서드
    public static void IoRun(ExecutorService es) {
        Instant start = Instant.now(); // 실행 시간 측정

        try (es) {
            IntStream.range(0, TASK_COUNT).forEach(idx -> {
                es.submit(() -> {
                    try {
                        // 실제 외부 API 및 DB 연동 코드(I/O 발생!)
                        // 가상 스레드는 i/o를 만나면 unmount가 되고
                        Thread.sleep(IO_DURATION);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            });
        } // try-resource 자동으로 리소스 해제(es.close)

        Instant end = Instant.now(); // 종료 시간 측정
        System.out.printf("I/O 작업 완료 시간: %d ms%n", Duration.between(start, end).toMillis());
    }

    // CPU 연산 작업 측정 메서드 (정국님의 주석 유지)
    public static void run(ExecutorService es) {
        Instant start = Instant.now(); // 실행 시간 측정

        try (es) {
            IntStream.range(0, TASK_COUNT).forEach(idx -> {
                es.submit(() -> {
                    // CPU 부하를 조금 더 늘려야 차이가 보입니다 (반복 횟수 증가)
                    for (int i = 0; i < 10_000_000; i++) {
                        int a = 1;
                        int b = 2;
                        int c = a + b;
                    }
                });
            });
        } // try-resource 자동으로 리소스 해제(es.close)

        Instant end = Instant.now(); // 종료 시간 측정
        System.out.printf("CPU 작업 완료 시간: %d ms%n", Duration.between(start, end).toMillis());
    }

    public static void ioRunPinning(ExecutorService es) {
        Instant start = Instant.now(); // 실행 시간 측정

        try (es) {
            IntStream.range(0, TASK_COUNT).forEach(idx -> {
                es.submit(() -> {
                    ReentrantLock lock = new ReentrantLock();
                    //내부적으로 락을 사용한다고 가정
                    //synchronized 커널의 세마포어/뮤텍스 객체를 동시성을 제어 ,SystemCall
                    //-> 플랫폼 스레드가 Blocked이 되고, 그러면 프랫폼 스레드 1번이 일을 못하게되니까 가상스레드도 일을 못하게됨
                    //가상 스레드 장점 없어지게되고, 비효율적으로 돌아가게됨.
                    synchronized (es) {
                        try {
                            //실제 외부 API 및 DB 연동 코드(i/o 발생!, i/o bound)
                            //가상 스레드는 i/o를 만나면 unmount되고 다른 가상스레드가 일을 할 수 있게 됨
                            Thread.sleep(IO_DURATION);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                });
            });
        } // try-resource 자동으로 리소스 해제(es.close)

        Instant end = Instant.now(); // 종료 시간 측정
        System.out.printf("I/O 작업 완료 시간: %d ms%n", Duration.between(start, end).toMillis());
    }

    //ReentrantLock 적용
    public static void ioRunPinningRL(ExecutorService es) {
        Instant start = Instant.now(); // 실행 시간 측정

        try (es) {
            IntStream.range(0, TASK_COUNT).forEach(idx -> {
                es.submit(() -> {
                    ReentrantLock lock = new ReentrantLock();
                    lock.lock();
                    try {
                        //실제 외부 API 및 DB 연동 코드(i/o 발생!, i/o bound)
                        //가상 스레드는 i/o를 만나면 unmount되고 다른 가상스레드가 일을 할 수 있게 됨
                        Thread.sleep(IO_DURATION);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        lock.unlock();
                    }
                });
            });
        } // try-resource 자동으로 리소스 해제(es.close)

        Instant end = Instant.now(); // 종료 시간 측정
        System.out.printf("I/O 작업 완료 시간: %d ms%n", Duration.between(start, end).toMillis());
    }
}