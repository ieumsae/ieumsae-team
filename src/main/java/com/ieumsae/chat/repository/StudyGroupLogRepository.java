package com.ieumsae.chat.repository;

import com.ieumsae.chat.domain.StudyGroupLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudyGroupLogRepository extends JpaRepository<StudyGroupLog, Long> {

    @Query("SELECT s.userIdx FROM StudyGroupLog s WHERE s.studyIdx = :studyIdx")
    Optional<Integer> findUserIdxByStudyIdx(Integer studyIdx);

}
