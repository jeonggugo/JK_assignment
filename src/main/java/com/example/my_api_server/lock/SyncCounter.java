package com.example.my_api_server.lock;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Getter
@Slf4j
public class SyncCounter {

    private int count = 0; //해당 공유 영역 값(Heap)을 동시에 수정

    public static void main(String[] args) {
        List<Thread> threads = new ArrayList<>();
        int threadCount = 5;//유저수라고 생각하면 됨.
        SyncCounter counter = new SyncCounter();

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

    //메서드 단위의 sync 메서드 실행자체에 대해서 락을 얻어서 순서를 제어합니다.

    private synchronized void increaseCount() {
        //스레드 N번이 들어오면서 락을 흭득합니다.
        Thread.State state = Thread.currentThread().getState();
        log.info("state1 = {}", state.toString());
//        synchronized (this) { //락으로 순서를 제어하겠다.
//            log.info("state2(락을 얻는부분) = {}", state.toString());
//            count++;
//        }
        //스레드 N번이 나가면서 락을 반환합니다.
        log.info("state3 = {}", state.toString());
    }

}



