package com.ieumsae.chat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table (name = "STUDY_GROUP_LOG")
public class StudyGroupLog {

    @Id
    @Column(name = "idx")
    private Long idx;

    @Column(name = "study_idx")
    private int studyIdx;

    @Column(name = "user_idx")
    private int userIdx;
}
