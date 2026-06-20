package com.aireader.v2.repository;

import com.aireader.v2.model.entity.ChapterFact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChapterFactRepository extends JpaRepository<ChapterFact, Long> {
    
    List<ChapterFact> findByNovelId(String novelId);
    
    Optional<ChapterFact> findByNovelIdAndChapterId(String novelId, Long chapterId);
    
    List<ChapterFact> findByChapterId(Long chapterId);

    /**
     * 根据小说ID和章节号查询事实（章节号是章节在小说中的编号，不是数据库ID）
     */
    @Query("SELECT cf FROM ChapterFact cf WHERE cf.novelId = :novelId AND cf.chapterId = :chapterNum")
    List<ChapterFact> findByNovelIdAndChapterId(@Param("novelId") String novelId, @Param("chapterNum") int chapterNum);

    void deleteByNovelIdAndChapterId(String novelId, Long chapterId);

    void deleteByChapterId(Long chapterId);
}
