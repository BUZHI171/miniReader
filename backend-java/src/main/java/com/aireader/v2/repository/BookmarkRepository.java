package com.aireader.v2.repository;

import com.aireader.v2.model.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    List<Bookmark> findByNovelIdOrderByCreatedAtDesc(String novelId);

    void deleteById(Long id);
}
