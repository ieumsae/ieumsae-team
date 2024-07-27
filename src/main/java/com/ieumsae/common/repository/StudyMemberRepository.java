package com.ieumsae.common.repository;

import com.ieumsae.common.entity.StudyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {

    // STUDY_MEMBER 테이블에서 studyIdx에 관련된 레코드를 삭제하는 메소드
    void deleteByStudyId(Long studyId);

    // STUDY_MEMBER 테이블에서 study_idx와 user_idx로 신청했던 기록을 찾는다. -> 스터디 신청 거절 로직
    Optional<StudyMember> findByStudyIdAndUserId(Long studyId, Long userId);

}
