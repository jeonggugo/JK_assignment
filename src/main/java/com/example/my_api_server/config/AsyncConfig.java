package com.example.my_api_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class AsyncConfig {
    //i/o바운드
    @Bean("ioExecutor")
    public ExecutorService ioExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    //스레드 개수는 막 넣는게 아니다(Context Switch Cost 비례)
    //그래서 논리적으로 계산해서 넣습니다!
    //cpu 바운드
    @Bean("cpuExecutor")
    public ExecutorService cpuExecutor() {
        //cpu 코어 개수 확인
        //인텔에는 하이퍼 스레딩이라는 기술이 있음
        int coreCount = Runtime.getRuntime().availableProcessors();
        return Executors.newFixedThreadPool(coreCount);
    }
}
