package com.aireader.v2.repository;

import com.aireader.v2.model.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationIdOrderByIdAsc(String conversationId);

    List<Message> findTop10ByConversationIdOrderByIdDesc(String conversationId);

    @Query("SELECT m FROM Message m WHERE m.conversationId = :conversationId ORDER BY m.id ASC LIMIT :limit")
    List<Message> findByConversationIdWithLimit(@Param("conversationId") String conversationId, @Param("limit") int limit);
}
