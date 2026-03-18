package com.example.my_api_server;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

//실제 스프링에게 빈(객체)으로 등록하게 해주는 설정
//IOC 컨테이너에 등록이된다. (객체=물건, 단 하나만 생성이된다!, 싱글톤 패턴이라고 함)
@Component

public class IOC {
   // @Bean// 보통 Bean은 메서드단위로 등록한다. 메서드의 리턴 타입 만약 void가 아닌 IOC_TEST로 했을 때, IOC 컨테이너에 반환해주는것이다.
    public void func1(){
        System.out.println("func1 실행");
    }
        public static void main(String[] args) {
        //객체 생성
        //메모리 (RAM), JVM Heap 메모리에 사용한다.
        // 방이 5평인데 물건을 계속 들여오면 OOM(Out of Heap memory)발생

        //Spring한테 우리가 IOC라는 객체를 만들어줄테니, 대신에 하나로만 만들어서재사용하게해줘->이것이 IOC(제어의 역전)개념!
        //개발자가 직접 객체를 만드는게 아니라, 스프링 스프링프레임워크가 관리해주는 것 ! 필요할 때 스프링이 주입해준다(DI)
        IOC ioc =new IOC();

        //객체의 메서드 호출
        ioc.func1();
    }

}
