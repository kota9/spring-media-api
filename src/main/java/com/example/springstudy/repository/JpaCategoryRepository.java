package com.example.springstudy.repository;

import com.example.springstudy.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCategoryRepository extends JpaRepository<Category, Long> {
}
