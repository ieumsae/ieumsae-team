package com.ieumsae.chat.repository;

import com.ieumsae.common.entity.StudyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {
    
    // 채팅방 입장 시에 스터디 구성원인지 확인하는 로직
    // userId와 studyId가 매칭되면서 status가 true인 사람만 입장 가능
    Optional<StudyMember> findByStudyIdAndUserId(Long studyId, Long userId);
}
