package com.github.stone;

import com.github.stone.entity.TodoV2;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class ValidateTest {


    @Test
    public void test() {

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Set<ConstraintViolation<TodoV2>> violations = validator.validate(TodoV2.builder().build());

        System.out.println("=== show error ===");
        violations.forEach(System.out::println);
    }

}
