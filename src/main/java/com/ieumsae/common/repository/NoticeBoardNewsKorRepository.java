package com.ieumsae.common.repository;

import com.ieumsae.common.entity.NoticeBoardNewsKor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeBoardNewsKorRepository extends JpaRepository<NoticeBoardNewsKor, Long> {
    List<NoticeBoardNewsKor> findTop10ByOrderByPostingDateDesc();
}
