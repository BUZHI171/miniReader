package com.aireader.v2.repository;

import com.aireader.v2.model.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 章节数据访问层
 */
@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    
    List<Chapter> findByNovelIdOrderByChapterNum(String novelId);
    
    Optional<Chapter> findByNovelIdAndChapterNum(String novelId, Integer chapterNum);
    
    @Query("SELECT COUNT(c) FROM Chapter c WHERE c.novelId = :novelId")
    Long countByNovelId(@Param("novelId") String novelId);
    
    @Query("SELECT COUNT(c) FROM Chapter c WHERE c.novelId = :novelId AND c.analysisStatus = 'completed'")
    Long countCompletedByNovelId(@Param("novelId") String novelId);
    
    @Query("SELECT c FROM Chapter c WHERE c.novelId = :novelId AND c.analysisStatus = 'failed'")
    List<Chapter> findFailedByNovelId(@Param("novelId") String novelId);
}
