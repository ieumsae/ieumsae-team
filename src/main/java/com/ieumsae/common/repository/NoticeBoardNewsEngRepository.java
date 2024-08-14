package com.ieumsae.common.repository;

import com.ieumsae.common.entity.NoticeBoardNewsEng;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeBoardNewsEngRepository extends JpaRepository<NoticeBoardNewsEng, Long> {
    List<NoticeBoardNewsEng> findTop10ByOrderByPostingDateDesc();
}
