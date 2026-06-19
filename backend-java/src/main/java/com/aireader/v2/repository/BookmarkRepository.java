package com.aireader.v2.repository;

import com.aireader.v2.model.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    //@Query("SELECT b FROM Bookmark b WHERE b.novelId = :novelId ORDER BY b.createdAt DESC")
    List<Bookmark> findByNovelIdOrderByCreatedAtDesc(@Param("novelId") String novelId);

    void deleteById(Long id);
}
