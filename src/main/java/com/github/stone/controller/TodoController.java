package com.github.stone.controller;

import com.github.stone.entity.TodoV1;
import com.github.stone.entity.TodoV2;
import com.github.stone.entity.TodoV3;
import com.github.stone.repository.TodoV1Repository;
import com.github.stone.repository.TodoV2Repository;
import com.github.stone.repository.TodoV3Repository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static com.github.stone.controller.GenericResponse.SUCCESS_CODE;

@RestController
@RequiredArgsConstructor
public class TodoController {

    private final TodoV1Repository todoV1Repository;
    private final TodoV2Repository todoV2Repository;
    private final TodoV3Repository todoV3Repository;

    @PostMapping("/v1/todo")
    public GenericResponse<TodoV1> createTodoV1(@RequestBody CreateTodoDto dto) {

        // expected org.springframework.dao.DataIntegrityViolationException

        return new GenericResponse(SUCCESS_CODE,
                todoV1Repository.save(TodoV1.builder()
                        .name(dto.getName())
                        .description(dto.getDescription())
                        .build()));
    }

    @PostMapping("/v2/todo")
    public GenericResponse<TodoV2> createTodoV2(@RequestBody CreateTodoDto dto) {

        // expected jakarta.validation.ConstraintViolationException

        return new GenericResponse(SUCCESS_CODE,
                todoV2Repository.save(TodoV2.builder()
                        .name(dto.getName())
                        .description(dto.getDescription())
                        .build()));
    }

    @PostMapping("/v3/todo")
    public GenericResponse<TodoV3> createTodoV3(@Valid @RequestBody CreateTodoDto dto) {

        // expected org.springframework.web.bind.MethodArgumentNotValidException

        return new GenericResponse(SUCCESS_CODE,
                todoV3Repository.save(TodoV3.builder()
                        .name(dto.getName())
                        .description(dto.getDescription())
                        .build()));
    }

    @Data
    public static class CreateTodoDto {

        @NotNull
        @Size(max = 10)
        private String name;

        @NotNull
        @Size(max = 20)
        private String description;
    }

}

