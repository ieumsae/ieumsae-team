package com.ieumsae.notice.service;

import com.ieumsae.common.entity.NoticeBoardNewsEng;
import com.ieumsae.common.repository.NoticeBoardNewsEngRepository;
import com.ieumsae.notice.dto.NoticeBoardNewsEngDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoticeBoardNewsEngService {

    @Autowired
    private NoticeBoardNewsEngRepository noticeBoardNewsEngRepository;

    public List<NoticeBoardNewsEngDTO> getRecentNotices(int limit) {
        List<NoticeBoardNewsEng> notices = noticeBoardNewsEngRepository.findTop10ByOrderByPostingDateDesc();
        return notices.stream().map(notice ->
                new NoticeBoardNewsEngDTO(notice.getNewsEngId(), notice.getNewsTitle(), notice.getPostingDate(), notice.getLink())
        ).limit(limit).collect(Collectors.toList());
    }
}