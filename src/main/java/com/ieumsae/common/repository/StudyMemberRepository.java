package com.ieumsae.common.repository;

import com.ieumsae.common.entity.StudyMember;
import com.ieumsae.study.study.dto.StudyMemberDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {

    // STUDY_MEMBER 테이블에서 studyIdx에 관련된 레코드를 삭제하는 메소드
    void deleteByStudyId(Long studyId);

    // STUDY_MEMBER 테이블에서 study_idx와 user_idx로 신청했던 기록을 찾는다. -> 스터디 신청 거절 로직
    Optional<StudyMember> findByStudyIdAndUserId(Long studyId, Long userId);

    List<StudyMember> findByStudyIdAndStatusFalse(Long studyId);

    @Query("SELECT new com.ieumsae.study.study.dto.StudyMemberDTO(sm.studyMemberId, sm.studyId, sm.userId, sm.status, u.nickname) " +
            "FROM StudyMember sm JOIN User u ON sm.userId = u.userId " +
            "WHERE sm.studyId = :studyId AND sm.status = false")
    List<StudyMemberDTO> findPendingMembersByStudyId(@Param("studyId") Long studyId);

}
