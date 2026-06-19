package com.aireader.v2.repository;

import com.aireader.v2.model.entity.EntityDictionary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntityDictionaryRepository extends JpaRepository<EntityDictionary, Long> {

    @Query("SELECT e FROM EntityDictionary e WHERE e.novelId = :novelId ORDER BY e.frequency DESC")
    List<EntityDictionary> findAllByNovelIdOrderByFrequencyDesc(@Param("novelId") String novelId);

    @Query("SELECT e FROM EntityDictionary e WHERE e.novelId = :novelId AND e.entityType = :entityType ORDER BY e.frequency DESC LIMIT :limit")
    List<EntityDictionary> findByNovelIdAndEntityTypeOrderByFrequencyDesc(@Param("novelId") String novelId, @Param("entityType") String entityType, @Param("limit") int limit);

    @Modifying
    @Query("DELETE FROM EntityDictionary e WHERE e.novelId = :novelId")
    int deleteByNovelId(@Param("novelId") String novelId);

    @Query("SELECT COUNT(e) FROM EntityDictionary e WHERE e.novelId = :novelId")
    long countByNovelId(@Param("novelId") String novelId);
}