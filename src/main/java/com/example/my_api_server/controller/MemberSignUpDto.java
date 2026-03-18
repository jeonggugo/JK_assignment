package com.example.my_api_server.controller;

//record는 객체를 조금 더 사용하기 쉽게 하는 자바의 문법이다.

public record MemberSignUpDto(String email, String password) {

}
