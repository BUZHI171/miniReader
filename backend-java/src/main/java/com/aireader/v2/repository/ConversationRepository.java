package com.aireader.v2.repository;

import com.aireader.v2.model.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {

    List<Conversation> findByNovelIdOrderByUpdatedAtDesc(String novelId);

    Optional<Conversation> findById(String id);

    @Query(value = "SELECT c.*, (SELECT COUNT(*) FROM messages m WHERE m.conversation_id = c.id) as message_count " +
                   "FROM conversations c WHERE c.novel_id = :novelId ORDER BY c.updated_at DESC", 
            nativeQuery = true)
    List<Object[]> findConversationsWithMessageCount(@Param("novelId") String novelId);
}
