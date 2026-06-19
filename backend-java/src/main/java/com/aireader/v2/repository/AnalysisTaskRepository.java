package com.aireader.v2.repository;

import com.aireader.v2.model.entity.AnalysisTask;
import org.springframework.data.jpa.repository.JpaRepository;
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
    
    List<AnalysisTask> findByNovelId(String novelId);
    
    @Query("SELECT at FROM AnalysisTask at WHERE at.novelId = :novelId AND at.status = 'running'")
    Optional<AnalysisTask> findRunningTaskByNovelId(@Param("novelId") String novelId);
    
    @Query("SELECT at FROM AnalysisTask at WHERE at.novelId = :novelId ORDER BY at.createdAt DESC")
    List<AnalysisTask> findByNovelIdOrderByCreatedAtDesc(@Param("novelId") String novelId);
    
    @Query("SELECT at FROM AnalysisTask at WHERE at.novelId = :novelId AND at.status = 'paused'")
    Optional<AnalysisTask> findPausedTaskByNovelId(@Param("novelId") String novelId);
}
