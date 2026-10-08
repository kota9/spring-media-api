package com.example.springstudy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 애플리케이션 시작점.
 * 이 클래스가 있는 패키지(com.example.springstudy)와 그 하위 패키지를 컴포넌트 스캔한다.
 */
@SpringBootApplication
public class SpringMediaApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringMediaApiApplication.class, args);
    }
}
