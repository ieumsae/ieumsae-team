package com.ieumsae.study.study.service;

import com.ieumsae.common.entity.Study;
import com.ieumsae.common.entity.StudyMember;
import com.ieumsae.common.entity.User;
import com.ieumsae.common.repository.*;
import com.ieumsae.common.utils.SecurityUtils;
import com.ieumsae.study.study.dto.StudyDTO;
import com.ieumsae.study.study.dto.StudyMemberDTO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StudyService {

    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ReviewRepository reviewRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    @Autowired
    public StudyService(StudyRepository studyRepository, StudyMemberRepository studyMemberRepository, ChatRoomRepository chatRoomRepository, ReviewRepository reviewRepository, ScheduleRepository scheduleRepository, UserRepository userRepository) {
        this.studyRepository = studyRepository;
        this.studyMemberRepository = studyMemberRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.reviewRepository = reviewRepository;
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
    }

    public List<StudyDTO> getAllStudies() {
        List<Study> studies = studyRepository.findAll();
        studies.sort(Comparator.comparing(Study::getStudyId).reversed());
        return studies.stream().map(study -> {
            User user = userRepository.findByUserId(study.getCreatorId());
            String nickname = (user != null && user.getNickname() != null) ? user.getNickname() : "Unknown";
            return new StudyDTO(study.getStudyId(), study.getTitle(), study.getContent(), study.getCreatedDt(), nickname, study.getCreatorId());
        }).collect(Collectors.toList());
    }

    // 스터디 개설
    @Transactional
    public void createStudy(StudyDTO studyDTO) {
        Study study = new Study();
        study.setTitle(studyDTO.getTitle());
        study.setCreatorId(studyDTO.getCreatorId());
        study.setContent(studyDTO.getContent());
        study.setCreatedDt(LocalDateTime.now());

        Study savedStudy = studyRepository.save(study);

        StudyMember studyMember = new StudyMember();
        studyMember.setStudyId(savedStudy.getStudyId());
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
        StudyMember studyMember = studyMemberRepository.findById(studyMemberId)
                .orElseThrow(() -> new RuntimeException("스터디 멤버를 찾을 수 없습니다. ID: " + studyMemberId));

        Study study = studyRepository.findById(studyMember.getStudyId())
                .orElseThrow(() -> new RuntimeException("스터디를 찾을 수 없습니다. ID: " + studyMember.getStudyId()));

        if (!study.getCreatorId().equals(userId)) {
            throw new RuntimeException("사용자가 스터디 방장이 아닙니다.");
        }

        if (!studyMember.isStatus()) {
            studyMember.setStatus(true);
            studyMemberRepository.save(studyMember);
        } else {
            throw new RuntimeException("이미 승인된 멤버입니다.");
        }
    }


    // 스터디 신청 거절
    @Transactional
    public void rejectStudyApplication(Long studyId, Long applicantUserId, Long currentUserId) {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new RuntimeException("스터디를 찾을 수 없습니다. ID: " + studyId));

        if (!currentUserId.equals(study.getCreatorId())) {
            throw new RuntimeException("스터디 방장만 신청을 거절할 수 있습니다.");
        }

        StudyMember studyMember = studyMemberRepository.findByStudyIdAndUserId(studyId, applicantUserId)
                .orElseThrow(() -> new RuntimeException("해당 사용자의 스터디 신청 정보를 찾을 수 없습니다."));

        if (!studyMember.isStatus()) {
            studyMemberRepository.delete(studyMember);
        } else {
            throw new RuntimeException("이미 승인된 스터디 멤버는 거절할 수 없습니다.");
        }
    }

    @Transactional
    // 스터디 수정
    public void updatestudy(Long studyId ,StudyDTO studyDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new RuntimeException("커뮤니티를 찾을 수 없습니다."));

        // 커뮤니티 개설자만 수정할 수 있게 권한 부여
        if (study.getCreatorId().equals(userId)) {
            study.setTitle(studyDTO.getTitle());
            study.setContent(studyDTO.getContent());
            study.setCreatedDt(LocalDateTime.now());
            studyRepository.save(study);
        } else {
            throw new RuntimeException("커뮤니티 게시자가 아닙니다.");
        }

    }

    // 스터디 상세 조회 메서드
    public StudyDTO getStudyById(Long studyId) {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new RuntimeException("커뮤니티를 찾을 수 없습니다."));
        User user = userRepository.findByUserId(study.getCreatorId());
        String nickname = (user != null && user.getNickname() != null) ? user.getNickname() : "Unknown";
        return new StudyDTO(study.getStudyId(), study.getTitle(), study.getContent(), study.getCreatedDt(), nickname);
    }


    // 신청자 리스트 가져오기 (닉네임으로)
    public List<StudyMemberDTO> getPendingMembersWithNickname(Long studyId) {
        List<StudyMember> pendingMembers = studyMemberRepository.findByStudyIdAndStatusFalse(studyId);
        return pendingMembers.stream()
                .map(studyMember -> {
                    User user = userRepository.findByUserId(studyMember.getUserId());
                    return new StudyMemberDTO(studyMember.getStudyMemberId(), studyMember.getUserId(), user.getNickname(), false);
                })
                .collect(Collectors.toList());
    }
}
