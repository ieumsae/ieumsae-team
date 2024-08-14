package com.ieumsae.notice.service;

import com.ieumsae.common.entity.NoticeBoardNewsKor;
import com.ieumsae.common.repository.NoticeBoardNewsKorRepository;
import com.ieumsae.notice.dto.NoticeBoardNewsKorDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoticeBoardNewsKorService {

    @Autowired
    private NoticeBoardNewsKorRepository noticeBoardNewsKorRepository;

    public List<NoticeBoardNewsKorDTO> getRecentNotices(int limit) {
        List<NoticeBoardNewsKor> notices = noticeBoardNewsKorRepository.findTop10ByOrderByPostingDateDesc();
        return notices.stream().map(notice ->
                new NoticeBoardNewsKorDTO(notice.getNewsKorId(), notice.getNewsTitle(), notice.getPostingDate(), notice.getLink())
        ).limit(limit).collect(Collectors.toList());
    }
}