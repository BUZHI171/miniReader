package com.aireader.v2.repository;

import com.aireader.v2.model.entity.ChapterFact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChapterFactRepository extends JpaRepository<ChapterFact, Long> {
    
    List<ChapterFact> findByNovelId(String novelId);
    
    Optional<ChapterFact> findByNovelIdAndChapterId(String novelId, Long chapterId);
    
    List<ChapterFact> findByChapterId(Long chapterId);
}
