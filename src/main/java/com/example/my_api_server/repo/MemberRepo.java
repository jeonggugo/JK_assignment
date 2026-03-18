package com.example.my_api_server.repo;


import com.example.my_api_server.entity.Member;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

//db 통신 없이 인메모리 DB를 사용해서 CRUD를 간단하게 해볼거임.
//DAO: 실제 DB와 통신하는 부분임
@Component
public class MemberRepo {
    Map<Long, Member> members = new HashMap<>();

    //연산 (저장, 수정, 삭제, 조회)
    public Long saveMember(String email, String password) {
        Random random = new Random();
        long id = random.nextLong();
        Member member = Member.builder()
                .id(id)
                .email(email)
                .password(password)
                .build();
        members.put(id, member);
        return id;
    }

    //조회
    public Member findMember(Long id) {
        return members.get(id);
    }
}
