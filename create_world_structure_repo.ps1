$content = @'
package com.aireader.v2.repository;

import com.aireader.v2.model.entity.WorldStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 世界结构数据访问层
 */
@Repository
public interface WorldStructureRepository extends JpaRepository<WorldStructure, String> {
    
    Optional<WorldStructure> findByNovelId(String novelId);
}
'@

Set-Content -Path "e:\kaifa\other\miniReader\backend-java\src\main\java\com\aireader\v2\repository\WorldStructureRepository.java" -Value $content -Encoding UTF8