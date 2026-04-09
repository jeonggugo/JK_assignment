package com.example.my_api_server.controller;

import ch.qos.logback.core.util.StringUtil;
import com.example.my_api_server.service.MemberDBService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/member")
public class MemberController {

    private final MemberDBService memberDBService;

    //회원가입
    @PostMapping// POST 통신 특정 데이터를 보내겠다.(리소드 등록, 약속)
    public long singUp(@Validated @RequestBody MemberSignUpDto dto) {
        System.out.println("email =" + dto.email());
        System.out.println("password =" + dto.password());
        //validation 검증
        if (StringUtil.isNullOrEmpty(dto.email()) || StringUtil.isNullOrEmpty(dto.password())) {
            new RuntimeException("email or password가 빈 값이 되면 안됩니다.");
        }
        Long memberId = null;
        try {
            memberId = memberDBService.signUp(dto.email(), dto.password());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return memberId;
    }

    //테스트 메서드
    @GetMapping("/test")
    public void test() {
        memberDBService.tx1();
    }


    //회원조회
//    @GetMapping("/{id}")
//    public Member findMember(@PathVariable Long id) {
//        Member member = memberService.findMember(id);
//        return member;
//    }
}
