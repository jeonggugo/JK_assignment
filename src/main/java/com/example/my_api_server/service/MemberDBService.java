package com.example.my_api_server.service;

import com.example.my_api_server.entity.Member;
import com.example.my_api_server.event.MemberSignUpEvent;
import com.example.my_api_server.repo.MemberDBRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor//생성자 주입 DI
@Slf4j //로그
public class MemberDBService {
    private final MemberDBRepo memberDBRepo;
    private final MemberPointService memberPointService;
    private final ApplicationEventPublisher publisher;//이벤트를 보내줄 publisher
    //오늘 뭘 만들거냐! 어제와 동일하게
    //회원저장

    /**
     * 1. @Transactional은 AOP로 돌아가서 begin tran() commit()을 수행함
     * 2. DB에는 commit 명령어가 실행되어야 테이블에 반영됨
     **/
    @Transactional(/*rollbackFor = IOException.class*/)
    public Long signUp(String email, String password) throws IOException {
        Member member = Member.builder()
                .email(email)
                .password(password)
                .build();

        //저장
        Member savedMember = memberDBRepo.save(member);

        //변경지점이 되게 작아지게되는 유지보수성 Up(강결합 해결)
        publisher.publishEvent(new MemberSignUpEvent(savedMember.getId(), savedMember.getEmail()));//이벤트 발송

//        sendNotification(); //알림이 대기하는 시간을 1분 이상이라고하면...

//        memberPointService.changeAllUserData();

//        throw new IOException("외부 API 호출하다가 I/O 예외가 터짐");
        //I/O입섹션 우리측 문제는 아니고, 상대측 문제이기 때문에 상대측 서버가 헬스체크가 된다면, 다시 보내줘야하는 로직을 구성해야(재전송 로직)

        //DB에 저장하다가 뭔가 오류가 발생해서 예외가 터짐(Runtime 예외)
//        throw new RuntimeException("DB에 저장하다가 뭔가 오류가 발생해서 예외가 터짐");

        return savedMember.getId();
    }

    //이메일, 알림 가정
    public void sendNotification() {
        try {
            Thread.sleep(5000);//ms 단위여서 1000ms->1초
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("알림전송 완료");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void changeAllUserData() {
        List<Member> memberList = memberDBRepo.findAll();

        //뭔가 값을 바꿧다고 가정

    }

    @Transactional(propagation = Propagation.REQUIRED, timeout = 2)//기본에 있던 트랜잭션을 쓰겠다. 없으면 새로 생성할게.
    public void tx1() {
        List<Member> members = memberDBRepo.findAll();
        members.forEach(m -> {
            log.info("member Id = {}", m.getId());
            log.info("member Email = {}", m.getEmail());
        });

        memberPointService.changeAllUserData();
        memberPointService.timeout();

    }
}
