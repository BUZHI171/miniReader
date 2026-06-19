package com.aireader.v2.repository;

import com.aireader.v2.model.entity.WorldStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 世界结构数据访问层
 */
@Repository
public interface WorldStructureRepository extends JpaRepository<WorldStructure, String> {
    
    Optional<WorldStructure> findByNovelId(String novelId);
    
    void deleteByNovelId(String novelId);
}
