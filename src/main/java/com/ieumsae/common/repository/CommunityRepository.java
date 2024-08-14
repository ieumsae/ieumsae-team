
package com.ieumsae.common.repository;

import com.ieumsae.common.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Long> {
    
    // 최근 순으로 10개의 게시글을 조회
    List<Community> findTop10ByOrderByWriteDtDesc();

}
