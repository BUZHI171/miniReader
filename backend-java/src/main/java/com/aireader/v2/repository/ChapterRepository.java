package com.aireader.v2.repository;

import com.aireader.v2.model.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    /**
     * 查找失败的章节（别名方法）
     */
    default List<Chapter> findFailedChapters(String novelId) {
        return findFailedByNovelId(novelId);
    }

    // 原生SQL查询
    @Query(value = "SELECT id FROM chapters WHERE novel_id = :novelId AND chapter_num = :chapterNum", nativeQuery = true)
    List<Object[]> findChapterId(@Param("novelId") String novelId, @Param("chapterNum") Integer chapterNum);

    @Modifying
    @Query(value = "UPDATE chapters SET is_excluded = :isExcluded, analysis_status = :status, updated_at = :updatedAt WHERE novel_id = :novelId AND chapter_num = :chapterNum", nativeQuery = true)
    int updateChapterExcluded(@Param("novelId") String novelId, @Param("chapterNum") Integer chapterNum, @Param("isExcluded") String isExcluded, @Param("status") String status, @Param("updatedAt") String updatedAt);

    @Modifying
    @Query(value = "UPDATE chapters SET is_excluded = :isExcluded, updated_at = :updatedAt WHERE novel_id = :novelId AND chapter_num = :chapterNum", nativeQuery = true)
    int updateChapterExcludedOnly(@Param("novelId") String novelId, @Param("chapterNum") Integer chapterNum, @Param("isExcluded") String isExcluded, @Param("updatedAt") String updatedAt);

    /**
     * 更新章节分析状态（仅状态）
     */
    @Modifying
    @Query(value = "UPDATE chapters SET analysis_status = :status, updated_at = datetime('now') WHERE novel_id = :novelId AND chapter_num = :chapterNum", nativeQuery = true)
    void updateAnalysisStatus(@Param("novelId") String novelId, @Param("chapterNum") Integer chapterNum, @Param("status") String status);

    /**
     * 更新章节分析状态（带错误信息和错误类型）
     */
    @Modifying
    @Query(value = "UPDATE chapters SET analysis_status = :status, error_message = :errorMessage, error_type = :errorType, updated_at = datetime('now') WHERE novel_id = :novelId AND chapter_num = :chapterNum", nativeQuery = true)
    void updateAnalysisStatus(@Param("novelId") String novelId, @Param("chapterNum") Integer chapterNum, @Param("status") String status, @Param("errorMessage") String errorMessage, @Param("errorType") String errorType);
}
