package com.example.my_api_server.lock;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Getter
@Slf4j
public class ReentrantCounter {
    private final ReentrantLock lock = new ReentrantLock();

    private int count = 0; //해당 공유 영역 값(Heap)을 동시에 수정

    public static void main(String[] args) {
        List<Thread> threads = new ArrayList<>();
        int threadCount = 1000;//유저수라고 생각하면 됨.
        ReentrantCounter counter = new ReentrantCounter();

        //스레드 생성
        for (int i = 0; i < threadCount; i++) {
            Thread thread = new Thread(counter::increaseCount);//스레드 연산
            thread.start();//스레드 시작
            threads.add(thread);//스레드 그룹에 스레드 add

        }
        //스레드가 일이 다 끝날 때까지 기다린다.
        threads.forEach(thread -> {
            try {
                thread.join();//스레드가 종료될 때까지 기다림
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        log.info("기대값 : {}", threadCount);
        log.info("실제값 : {}", counter.count);
    }

    private void increaseCount() {
        this.lock.lock();
        try {
            if (this.lock.tryLock(3, TimeUnit.SECONDS)) {
                try {
                    log.info("락 획득 후 연산 작업 시작!");
                    this.count++;
                    Thread.sleep(4000);
                } finally {
                    this.lock.unlock();
                }
            } else {
                log.info("3초 안에 락을 획득하지 못함");
            }
        } catch (InterruptedException e) {
            log.info("작업 중단!");
            throw new RuntimeException(e);
        } finally {
            this.lock.unlock(); // 바깥 lock.lock()에 대한 반납
        }
    }
}



