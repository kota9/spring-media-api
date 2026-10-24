package com.example.springstudy.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MemberCreateRequestTest {
    private static Validator validator;
    private static jakarta.validation.ValidatorFactory validatorFactory;

    @BeforeAll
    static void createValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void acceptsValidRequest() {
        assertThat(validator.validate(new MemberCreateRequest("홍길동", "member@example.com", "password123")))
                .isEmpty();
    }

    @Test
    void rejectsBlankAndWhitespaceOnlyNames() {
        assertViolation(new MemberCreateRequest("", "member@example.com", "password123"), "name", "이름은 필수입니다.");
        assertViolation(new MemberCreateRequest("   ", "member@example.com", "password123"), "name", "이름은 필수입니다.");
    }

    @Test
    void rejectsNameShorterThanTwoCharacters() {
        assertViolation(new MemberCreateRequest("김", "member@example.com", "password123"), "name", "이름은 2~20자여야 합니다.");
    }

    @Test
    void rejectsNameLongerThanTwentyCharacters() {
        assertViolation(new MemberCreateRequest("가".repeat(21), "member@example.com", "password123"), "name", "이름은 2~20자여야 합니다.");
    }

    @Test
    void rejectsMalformedEmail() {
        assertViolation(new MemberCreateRequest("홍길동", "wrong-email", "password123"), "email", "올바른 이메일 형식이어야 합니다.");
    }

    @Test
    void rejectsPasswordShorterThanEightCharacters() {
        assertViolation(new MemberCreateRequest("홍길동", "member@example.com", "1234567"), "password", "비밀번호는 8~100자여야 합니다.");
    }

    @Test
    void rejectsBlankPassword() {
        assertViolation(new MemberCreateRequest("홍길동", "member@example.com", ""), "password", "비밀번호는 필수입니다.");
    }

    private void assertViolation(MemberCreateRequest request, String field, String message) {
        Set<ConstraintViolation<MemberCreateRequest>> violations = validator.validate(request);
        assertThat(violations)
                .anySatisfy(violation -> {
                    assertThat(violation.getPropertyPath().toString()).isEqualTo(field);
                    assertThat(violation.getMessage()).isEqualTo(message);
                });
    }
}
