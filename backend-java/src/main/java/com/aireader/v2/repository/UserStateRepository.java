package com.aireader.v2.repository;

import com.aireader.v2.model.entity.UserState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户状态数据访问层
 */
@Repository
public interface UserStateRepository extends JpaRepository<UserState, String> {
    
    Optional<UserState> findByNovelId(String novelId);
}
