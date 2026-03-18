package com.example.my_api_server;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController//컨트롤러로 등록하겠다라는 의미
@RequestMapping("/test")// api 서버 주소
@RequiredArgsConstructor // DI를 자동으로 해주는 어노테이션(생성자 주입 방식을 어노테이션으로 사용하는 방법)
public class IOC_TEST {

    //1. 필드 주입(잘 사용 안함!)
//    @Autowired
//    private IOC ioc2;
//
//    //2. Setter(수정자) 주입 방식(잘안써요!)
//    public IOC setIoc(IOC ioc){
//        ioc2 = ioc;
//        return ioc;
//    }
//
//    //3. 생성자 주입 방식(생성할 때 주입 받는다, 주로 많이 사용하는 방식)
//    public void setIOC(IOC ioc){
//        ioc2 = ioc;
//    }

    //final: 불변성 객체를 변경할 수 없음
    private final IOC ioc;//개발자가 만든 것이 아닌 스프링이 객체(bean)를 주입해줬다(DI). -> ioc (제어의 역전)


    @GetMapping
    public void iocTest(){
        ioc.func1();
    }



}
