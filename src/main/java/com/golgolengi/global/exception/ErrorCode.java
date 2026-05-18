package com.golgolengi.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 인증
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    LOGOUT_TOKEN(HttpStatus.UNAUTHORIZED, "로그아웃된 토큰입니다."),

    // 회원
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),

    // 가족
    FAMILY_NOT_FOUND(HttpStatus.NOT_FOUND, "가족 그룹을 찾을 수 없습니다."),
    INVALID_INVITE_CODE(HttpStatus.BAD_REQUEST, "유효하지 않은 초대 코드입니다."),
    ALREADY_IN_FAMILY(HttpStatus.CONFLICT, "이미 가족 그룹에 속해 있습니다."),

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 입력입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;
}