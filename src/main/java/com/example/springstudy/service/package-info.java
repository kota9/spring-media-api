/**
 * 비즈니스 로직 계층.
 * Controller로부터 요청을 위임받아 규칙을 처리하고, Repository를 호출해 데이터를 다룬다.
 * 트랜잭션 경계(@Transactional)가 보통 이 계층에 위치한다.
 * 예) MediaService
 */
package com.example.springstudy.service;
