package com.example.my_api_server.service;

import com.example.my_api_server.entity.Member;
import com.example.my_api_server.repo.MemberDBRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberPointService {
    private final MemberDBRepo memberDBRepo;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void changeAllUserData() {
        List<Member> memberList = memberDBRepo.findAll();

        //뭔가 값을 바꿧다고 가정

    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void supportTxTest() {
        //db를 사용하지 않는 단순 자바코드 실행이거나, 혹은 readonly=true를 사용해서 주로 최적화된 읽기를 사용할 때, 가끔 사용
        memberDBRepo.findAll();

    }

    @Transactional(timeout = 2)//해당 시간 내에 트랜잭션이 실행되지 않으면, 예외발생
    public void timeout() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        memberDBRepo.findAll();
    }
}
