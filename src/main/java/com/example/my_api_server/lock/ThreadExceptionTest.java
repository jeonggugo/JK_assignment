package com.example.my_api_server.lock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadExceptionTest {
    private int count = 0;

    public static void main() {
        ThreadExceptionTest t = new ThreadExceptionTest();
        int threadCount = 10000;
        ExecutorService es = Executors.newFixedThreadPool(threadCount);// N개의 플랫폼 스레드 생성
        for (int i = 0; i < threadCount; i++) {
            es.submit(t::increase);
            System.out.println("실행완료!");
        }
    }

    public void increase() {
        count++;
    }
}
