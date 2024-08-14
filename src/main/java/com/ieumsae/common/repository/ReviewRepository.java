package com.ieumsae.common.repository;

import com.ieumsae.common.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // REVIEW 테이블에서 studyIdx에 관련된 레코드를 삭제하는 메소드
    void deleteByStudyId(Long studyId);
}
