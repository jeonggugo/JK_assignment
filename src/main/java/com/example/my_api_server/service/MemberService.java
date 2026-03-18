package com.example.my_api_server.service;
//실제 빚3ㅡ니스 로직이 구성됨

import com.example.my_api_server.entity.Member;
import com.example.my_api_server.repo.MemberRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service//Bean으로 등록
@RequiredArgsConstructor//생성자 주입 DI
@Slf4j//로그
public class MemberService {
    private final MemberRepo memberRepo;

    //회원 가입
    public Long signUp(String email, String password) {
        //회원 저장 후 알림을 전송한다.
        Long memberId = memberRepo.saveMember(email, password);

        log.info("회원가입한 member ID= {}", memberId);

        //알림 전송
        sendNotification();
        return memberId;
    }

    // 알림 외부 API 호출(TCP <-> HTTP 통신하게 됩니다.)
    //보통이렇게 되면 스레드가 블라킹이 되서 잠시 멈추는데 그걸 구현하기 위해 sleep을 사용함.
    public void sendNotification() {
        try {
            Thread.sleep(1000);//ms 단위여서 1000ms->1초
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("알림전송 완료");
    }

    //회원 조회
    public Member findMember(Long id) {
        Member member = memberRepo.findMember(id);
        return member;
    }
}
