package com.aireader.v2.repository;

import com.aireader.v2.model.entity.AnalysisTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 分析任务数据访问层
 */
@Repository
public interface AnalysisTaskRepository extends JpaRepository<AnalysisTask, String> {
    
    Optional<AnalysisTask> findByNovelId(String novelId);
    
    List<AnalysisTask> findByStatus(String status);
    
    List<AnalysisTask> findByNovelIdAndStatus(String novelId, String status);

    /**
     * 查找正在运行的任务
     */
    @Query("SELECT t FROM AnalysisTask t WHERE t.novelId = :novelId AND t.status IN ('running', 'paused')")
    Optional<AnalysisTask> findRunningTaskByNovelId(@Param("novelId") String novelId);

    /**
     * 更新任务状态
     */
    @Modifying
    @Query("UPDATE AnalysisTask t SET t.status = :status WHERE t.id = :taskId")
    void updateStatus(@Param("taskId") String taskId, @Param("status") String status);

    /**
     * 更新任务进度
     */
    @Modifying
    @Query("UPDATE AnalysisTask t SET t.currentChapter = :chapter WHERE t.id = :taskId")
    void updateProgress(@Param("taskId") String taskId, @Param("chapter") int chapter);

    /**
     * 查找最新任务
     */
    @Query("SELECT t FROM AnalysisTask t WHERE t.novelId = :novelId ORDER BY t.createdAt DESC")
    Optional<AnalysisTask> findLatestTask(@Param("novelId") String novelId);

    /**
     * 保存计时摘要
     */
    @Modifying
    @Query("UPDATE AnalysisTask t SET t.timingSummary = :timingSummary WHERE t.id = :taskId")
    void saveTimingSummary(@Param("taskId") String taskId, @Param("timingSummary") String timingSummary);
}
