package com.linkly.category;

import com.linkly.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * 카테고리 이름으로 조회
     */
    Optional<Category> findByName(String name);

    /**
     * 카테고리 이름 존재 여부 확인
     */
    boolean existsByName(String name);

    /**
     * 삭제되지 않은 카테고리 조회
     */
    Optional<Category> findByIdAndDeletedAtIsNull(Long id);

    /**
     * 삭제되지 않은 모든 카테고리 조회
     */
    List<Category> findAllByDeletedAtIsNull();

    /**
     * 삭제되지 않은 카테고리를 이름으로 조회
     */
    Optional<Category> findByNameAndDeletedAtIsNull(String name);
}