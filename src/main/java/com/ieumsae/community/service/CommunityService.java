package com.ieumsae.community.service;

import com.ieumsae.common.entity.Community;
import com.ieumsae.common.entity.User;
import com.ieumsae.common.repository.CommunityRepository;
import com.ieumsae.common.repository.UserRepository;
import com.ieumsae.common.utils.SecurityUtils;
import com.ieumsae.community.dto.CommunityDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;

    @Autowired
    public CommunityService(CommunityRepository communityRepository, UserRepository userRepository) {
        this.communityRepository = communityRepository;
        this.userRepository = userRepository;
    }

    // 커뮤니티 생성
    public void createCommunity(CommunityDTO communityDTO) {

        Long userId = SecurityUtils.getCurrentUserId();
        Community community = new Community();

        community.setTitle(communityDTO.getTitle());
        community.setContent(communityDTO.getContent());
        community.setWriteDt(LocalDateTime.now());
        community.setUserId(userId);

        communityRepository.save(community);
    }

    // 커뮤니티 삭제
    public void deleteCommunity(Long communityId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("커뮤니티를 찾을 수 없습니다."));

        // 커뮤니티 개설자만 삭제할 수 있게 권한 부여
        if (community.getUserId().equals(userId)) {
            communityRepository.delete(community);
        } else {
            throw new RuntimeException("커뮤니티 게시자가 아닙니다.");
        }
    }

    // 커뮤니티 수정
    public void updateCommunity(Long communityId ,CommunityDTO communityDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("커뮤니티를 찾을 수 없습니다."));

        // 커뮤니티 개설자만 수정할 수 있게 권한 부여
        if (community.getUserId().equals(userId)) {
            community.setTitle(communityDTO.getTitle());
            community.setContent(communityDTO.getContent());
            community.setWriteDt(LocalDateTime.now());
            communityRepository.save(community);
        } else {
            throw new RuntimeException("커뮤니티 게시자가 아닙니다.");
        }

    }

    // 커뮤니티 목록 조회 메서드
    public List<CommunityDTO> getAllCommunities() {
        List<Community> communities = communityRepository.findAll();
        // 커뮤니티 리스트를 내림차순으로 정렬
        communities.sort(Comparator.comparing(Community::getCommunityId).reversed());

        return communities.stream().map(community -> {
            User user = userRepository.findByUserId(community.getUserId());
            String nickname = user.getNickname();
            return new CommunityDTO(community.getCommunityId(), community.getTitle(), community.getContent(), community.getWriteDt(), nickname);
        }).collect(Collectors.toList());
    }

    // 커뮤니티 상세 조회 메서드
    public CommunityDTO getCommunityById(Long communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("커뮤니티를 찾을 수 없습니다."));
        User user = userRepository.findByUserId(community.getUserId());
        String nickname = user.getNickname();
        return new CommunityDTO(community.getCommunityId(), community.getTitle(), community.getContent(), community.getWriteDt(), nickname);
    }

    // 메인페이지에 커뮤니티 글 10개를 띄워주는 메소드
    public List<CommunityDTO> getRecentCommunities(int limit) {
        List<Community> communities = communityRepository.findTop10ByOrderByWriteDtDesc();
        return communities.stream().map(community -> {
            User user = userRepository.findByUserId(community.getUserId());
            String nickname = user.getNickname();
            return new CommunityDTO(community.getCommunityId(), community.getTitle(), community.getContent(), community.getWriteDt(), nickname);
        }).limit(limit).collect(Collectors.toList());
    }
}
