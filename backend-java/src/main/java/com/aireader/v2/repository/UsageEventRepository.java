package com.aireader.v2.repository;

import com.aireader.v2.model.entity.UsageEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 使用事件数据访问层
 */
@Repository
public interface UsageEventRepository extends JpaRepository<UsageEvent, Long> {

    /**
     * 获取事件类型统计
     */
    @Query(value = "SELECT event_type, COUNT(*) as count FROM usage_events " +
            "WHERE created_at >= datetime('now', :daysParam) " +
            "GROUP BY event_type ORDER BY count DESC", nativeQuery = true)
    List<Object[]> getEventStats(@Param("daysParam") String daysParam);

    /**
     * 获取每日趋势
     */
    @Query(value = "SELECT DATE(created_at) as day, COUNT(*) as count FROM usage_events " +
            "WHERE created_at >= datetime('now', :daysParam) " +
            "GROUP BY day ORDER BY day", nativeQuery = true)
    List<Object[]> getDailyTrend(@Param("daysParam") String daysParam);

    /**
     * 获取总事件数
     */
    @Query("SELECT COUNT(e) FROM UsageEvent e")
    long getTotalCount();

    /**
     * 清除所有事件
     */
    @Modifying
    @Query("DELETE FROM UsageEvent e")
    int clearAllEvents();
}
