package com.github.stone.repository;

import com.github.stone.entity.TodoV1;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoV1Repository extends JpaRepository<TodoV1, Integer> {
}
