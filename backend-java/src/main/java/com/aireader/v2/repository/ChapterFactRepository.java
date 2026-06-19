package com.aireader.v2.repository;

import com.aireader.v2.model.entity.ChapterFact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 章节事实数据访问层
 */
@Repository
public interface ChapterFactRepository extends JpaRepository<ChapterFact, Long> {
    
    Optional<ChapterFact> findByNovelIdAndChapterId(String novelId, Long chapterId);
    
    List<ChapterFact> findByNovelId(String novelId);
    
    @Query("SELECT cf FROM ChapterFact cf WHERE cf.novelId = :novelId ORDER BY cf.chapterId")
    List<ChapterFact> findAllByNovelIdOrderByChapterId(@Param("novelId") String novelId);
    
    void deleteByNovelId(String novelId);
}
