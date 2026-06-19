$content = @'
package com.aireader.v2.repository;

import com.aireader.v2.model.entity.AnalysisTask;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
'@

Set-Content -Path "e:\kaifa\other\miniReader\backend-java\src\main\java\com\aireader\v2\repository\AnalysisTaskRepository.java" -Value $content -Encoding UTF8