package com.ieumsae.common.repository;

import com.ieumsae.common.entity.Study;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyRepository extends JpaRepository<Study, Long>{

    // STUDY 테이블에서 studyId에 관련된 레코드를 삭제하는 메소드
    void deleteByStudyId(Long studyId);

    // STUDY 테이블에서 제목과 내용 중 아무거로나 검색하면 검색이 될 수 있게 하는 메소드
    // 부분 문자열을 포함한 검색기능으로 제목을 정확하게 일치시키지 않아도 검색 가능
    List<Study> findByTitleContainingOrContentContaining(String title, String content);
}
