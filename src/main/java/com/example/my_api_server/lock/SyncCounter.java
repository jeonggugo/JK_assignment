package com.example.my_api_server.lock;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Getter
@Slf4j
public class Counter {

    private int count = 0; //해당 공유 영역 값(Heap)을 동시에 수정

    public static void main(String[] args) {
        List<Thread> threads = new ArrayList<>();
        int threadCount = 30000;//유저수라고 생각하면 됨.
        Counter counter = new Counter();

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
        count++;
    }

}



