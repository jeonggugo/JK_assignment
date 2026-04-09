package com.example.my_api_server.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
public class MemberSignUpListener {
    //아직 새로운 일꾼을 안붙여서 일꾼 1번이 이 일을 진행할 예정.
    //이메일, 알림 가정
    //스레드 1번이 DB의 안정성(커밋) 확인되고나서 내 로직을 수행합니다.

    @Async//총 시간까지 개선하고 싶으면 다른 일꾼 2번에게 맡겨야합니다. @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)//DB가 커밋이 된 후에 이 로직을 수행해줘
    @Retryable(maxRetries = 3)//1. 알림을 실패하면 재시도 3번(결국 상대방 서버의 문제점이 해결될 때까지 시간이 소요됨)
    public void sendNotification(MemberSignUpEvent event) {
        log.info("member ID= {}", event.getId());
        log.info("member Email= {}", event.getEmail());

        try {
            Thread.sleep(5000);//ms 단위여서 1000ms->1초
        } catch (InterruptedException e) {
            //실패한 것들을 db에 저장했다가 나중에(특정 시간) 실패한것들을 한번에모아서 대량 알림을 발송 ->트랜잭션 아웃박스 패턴(실무)
            throw new RuntimeException(e);
        }
        log.info("알림전송 완료");
    }


}
