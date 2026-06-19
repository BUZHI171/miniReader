package com.aireader.v2.repository;

import com.aireader.v2.model.entity.Novel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 小说数据访问层
 */
@Repository
public interface NovelRepository extends JpaRepository<Novel, String> {
    
    List<Novel> findAll();
    
    Optional<Novel> findById(String id);
    
    void deleteById(String id);
    
    long count();
}
