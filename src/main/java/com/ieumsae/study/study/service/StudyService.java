package com.ieumsae.study.study.service;

import com.ieumsae.common.entity.Study;
import com.ieumsae.common.entity.StudyMember;
import com.ieumsae.common.entity.User;
import com.ieumsae.common.repository.*;
import com.ieumsae.common.utils.SecurityUtils;
import com.ieumsae.study.study.dto.StudyDTO;
import com.ieumsae.study.study.dto.StudyMemberDTO;
import jakarta.transaction.Transactional;
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

    public StudyService(StudyRepository studyRepository, StudyMemberRepository studyMemberRepository,
                        ChatRoomRepository chatRoomRepository, ReviewRepository reviewRepository,
                        ScheduleRepository scheduleRepository, UserRepository userRepository) {
        this.studyRepository = studyRepository;
        this.studyMemberRepository = studyMemberRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.reviewRepository = reviewRepository;
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
    }

    public List<StudyDTO> getAllStudies() {
        return studyRepository.findAll().stream()
                .sorted(Comparator.comparing(Study::getStudyId).reversed())
                .map(this::convertToStudyDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createStudy(StudyDTO studyDTO) {
        Study study = new Study();
        study.setTitle(studyDTO.getTitle());
        study.setCreatorId(studyDTO.getCreatorId());
        study.setContent(studyDTO.getContent());
        study.setCreatedDt(LocalDateTime.now());

        Study savedStudy = studyRepository.save(study);
        createStudyMember(savedStudy.getStudyId(), studyDTO.getCreatorId(), true);
    }

    @Transactional
    public void deleteStudy(Long studyId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Study study = findStudyById(studyId);

        if (!study.getCreatorId().equals(userId)) {
            throw new RuntimeException("스터디 방장이 아닙니다.");
        }

        studyMemberRepository.deleteByStudyId(studyId);
        scheduleRepository.deleteByStudyId(studyId);
        chatRoomRepository.deleteByStudyId(studyId);
        reviewRepository.deleteByStudyId(studyId);
        studyRepository.deleteByStudyId(studyId);
    }

    public void applyStudy(Long userId, Long studyId) {
        studyMemberRepository.findByUserIdAndStudyId(userId, studyId)
                .ifPresentOrElse(
                        member -> { throw new RuntimeException("이미 신청한 스터디입니다."); },
                        () -> createStudyMember(studyId, userId, false)
                );
    }

    @Transactional
    public void approveStudy(Long studyMemberId, Long userId) {
        StudyMember studyMember = findStudyMemberById(studyMemberId);
        Study study = findStudyById(studyMember.getStudyId());

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

    @Transactional
    public void rejectStudyApplication(Long studyId, Long applicantUserId, Long currentUserId) { // 리펙토링 예정
        Study study = findStudyById(studyId);

        if (!study.getCreatorId().equals(currentUserId)) {
            throw new RuntimeException("스터디 방장만 신청을 거절할 수 있습니다.");
        }

        StudyMember studyMember = findStudyMemberByStudyIdAndUserId(studyId, applicantUserId);

        if (!studyMember.isStatus()) {
            studyMemberRepository.delete(studyMember);
        } else {
            throw new RuntimeException("이미 승인된 스터디 멤버는 거절할 수 없습니다.");
        }
    }

    @Transactional
    public void updateStudy(Long studyId, StudyDTO studyDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        Study study = findStudyById(studyId);

        if (!study.getCreatorId().equals(userId)) {
            throw new RuntimeException("커뮤니티 게시자가 아닙니다.");
        }

        study.setTitle(studyDTO.getTitle());
        study.setContent(studyDTO.getContent());
        study.setCreatedDt(LocalDateTime.now());
        studyRepository.save(study);
    }

    public StudyDTO getStudyById(Long studyId) {
        Study study = findStudyById(studyId);
        return convertToStudyDTO(study);
    }

    public List<StudyMemberDTO> getPendingMembersWithNickname(Long studyId) {
        return studyMemberRepository.findByStudyIdAndStatusFalse(studyId).stream()
                .map(this::convertToStudyMemberDTO)
                .collect(Collectors.toList());
    }

    // Helper methods
    private Study findStudyById(Long studyId) {
        return studyRepository.findById(studyId)
                .orElseThrow(() -> new RuntimeException("스터디를 찾을 수 없습니다. ID: " + studyId));
    }

    private StudyMember findStudyMemberById(Long studyMemberId) {
        return studyMemberRepository.findById(studyMemberId)
                .orElseThrow(() -> new RuntimeException("스터디 멤버를 찾을 수 없습니다. ID: " + studyMemberId));
    }

    private StudyMember findStudyMemberByStudyIdAndUserId(Long studyId, Long userId) {
        return studyMemberRepository.findByStudyIdAndUserId(studyId, userId)
                .orElseThrow(() -> new RuntimeException("해당 사용자의 스터디 신청 정보를 찾을 수 없습니다."));
    }

    private void createStudyMember(Long studyId, Long userId, boolean status) {
        StudyMember studyMember = new StudyMember();
        studyMember.setStudyId(studyId);
        studyMember.setUserId(userId);
        studyMember.setStatus(status);
        studyMemberRepository.save(studyMember);
    }

    private StudyDTO convertToStudyDTO(Study study) {
        User user = userRepository.findByUserId(study.getCreatorId());
        String nickname = (user != null && user.getNickname() != null) ? user.getNickname() : "Unknown";
        return new StudyDTO(study.getStudyId(), study.getTitle(), study.getContent(), study.getCreatedDt(), nickname, study.getCreatorId());
    }

    private StudyMemberDTO convertToStudyMemberDTO(StudyMember studyMember) {
        User user = userRepository.findByUserId(studyMember.getUserId());
        String nickname = (user != null) ? user.getNickname() : "Unknown";
        return new StudyMemberDTO(studyMember.getStudyMemberId(), studyMember.getUserId(), nickname, studyMember.isStatus());
    }
}
