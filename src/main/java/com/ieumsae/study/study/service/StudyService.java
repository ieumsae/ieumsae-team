package com.ieumsae.study.study.service;

import com.ieumsae.common.entity.Study;
import com.ieumsae.common.entity.StudyMember;
import com.ieumsae.common.repository.*;
import com.ieumsae.common.utils.SecurityUtils;
import com.ieumsae.study.study.dto.StudyDTO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class StudyService {

    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ReviewRepository reviewRepository;
    private final ScheduleRepository scheduleRepository;

    @Autowired
    public StudyService(StudyRepository studyRepository, StudyMemberRepository studyMemberRepository, ChatRoomRepository chatRoomRepository, ReviewRepository reviewRepository, ScheduleRepository scheduleRepository) {
        this.studyRepository = studyRepository;
        this.studyMemberRepository = studyMemberRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.reviewRepository = reviewRepository;
        this.scheduleRepository = scheduleRepository;
    }

    // 스터디 개설
    public void createStudy(StudyDTO studyDTO) {
        Study study = new Study();
        // 스터디 관련 정보 저장 (테이블에 있는 칼럼)
        study.setTitle(studyDTO.getTitle());
        study.setCreatorId(studyDTO.getCreatorId());
        study.setContent(studyDTO.getContent());
        study.setCreatedDt(LocalDateTime.now());

        studyRepository.save(study);

        // 스터디 개설자 -> STUDY_MEMBER에 추가
        StudyMember studyMember = new StudyMember();
        studyMember.setStudyId(study.getStudyId());
        studyMember.setUserId(studyDTO.getCreatorId());
        studyMember.setStatus(true);

        studyMemberRepository.save(studyMember);
    }

    // 스터디 삭제
    // studyId를 받아서 관련된 레코드를 각 테이블에서 전부 삭제
    @Transactional
    public void deleteStudy(Long studyId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new RuntimeException("스터디를 찾을 수 없습니다."));
        // Optional 타입일 때, 예외처리 추가

        if (userId.equals(study.getCreatorId())) { // == 를 사용하면 값을 비교하는 것이 아니라 참조값을 비교하므로 논리상 불안정
            studyMemberRepository.deleteByStudyId(studyId);
            scheduleRepository.deleteByStudyId(studyId);
            chatRoomRepository.deleteByStudyId(studyId);
            reviewRepository.deleteByStudyId(studyId);
            studyRepository.deleteByStudyId(studyId);
        } else {
            throw new RuntimeException("스터디 방장이 아닙니다.");
        }
    }

    // 스터디 수정
    @Transactional
    public void updateStudy(Long studyId, StudyDTO studyDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new RuntimeException("스터디를 찾을 수 없습니다."));

        if (userId.equals(study.getCreatorId())) {
            study.setTitle(studyDTO.getTitle());
            study.setContent(studyDTO.getContent());
            studyRepository.save(study);
        } else {
            throw new RuntimeException("스터디 방장이 아닙니다.");
        }
    }

    // 스터디 신청
    // 스터디 신청단계 = STUDY_MEMBER 테이블의 status가 false인 데이터
    public void applyStudy(Long userId, Long studyId) {
        StudyMember studyMember = new StudyMember();
        studyMember.setStudyId(studyId);
        studyMember.setUserId(userId);
        studyMember.setStatus(false); // 기본값으로 false 설정

        studyMemberRepository.save(studyMember);
    }

    // 스터디 신청 승인
    // 스터디 신청 승인 단계 = STUDY_MEMBER 테이블의 status가 true로 업데이트
    @Transactional
    public void approveStudy(Long studyMemberId, Long userId) {
        Optional<StudyMember> optionalStudyMember = studyMemberRepository.findById(studyMemberId);
        if (optionalStudyMember.isPresent()) {
            StudyMember studyMember = optionalStudyMember.get();

            // 스터디 방장 확인
            Long studyId = studyMember.getStudyId();
            Optional<Study> optionalStudy = studyRepository.findById(studyId);
            if (optionalStudy.isPresent()) {
                Study study = optionalStudy.get();
                if (study.getCreatorId().equals(userId)) {
                    studyMember.setStatus(true); // status를 true로 변경
                    studyMemberRepository.save(studyMember);
                } else {
                    throw new RuntimeException("사용자가 스터디 방장이 아닙니다.");
                }
            } else {
                throw new RuntimeException("스터디를 찾을 수 없습니다. ID: " + studyId);
            }
        } else {
            throw new RuntimeException("스터디 멤버를 찾을 수 없습니다. ID: " + studyMemberId);
        }
    }

    // 스터디 신청 거절
    @Transactional
    public void rejectStudyApplication(Long studyId, Long applicantUserId) {
        // 현재 로그인한 사용자(스터디 방장)의 ID 가져오기
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 스터디 정보 조회
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new RuntimeException("스터디를 찾을 수 없습니다. ID: " + studyId));

        // 현재 사용자가 스터디 방장인지 확인
        if (!currentUserId.equals(study.getCreatorId())) {
            throw new RuntimeException("스터디 방장만 신청을 거절할 수 있습니다.");
        }

        // 해당 스터디 멤버 정보 조회
        StudyMember studyMember = studyMemberRepository.findByStudyIdAndUserId(studyId, applicantUserId)
                .orElseThrow(() -> new RuntimeException("해당 사용자의 스터디 신청 정보를 찾을 수 없습니다."));

        // status가 false인 경우(아직 승인되지 않은 경우)에만 삭제 진행
        if (!studyMember.isStatus()) {
            studyMemberRepository.delete(studyMember);
        } else {
            throw new RuntimeException("이미 승인된 스터디 멤버는 거절할 수 없습니다.");
        }
    }
}
