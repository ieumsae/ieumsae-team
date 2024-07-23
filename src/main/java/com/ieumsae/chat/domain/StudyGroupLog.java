package com.ieumsae.chat.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table (name = "STUDY_GROUP_LOG")
public class StudyGroupLog {

    @Id
    @Column(name = "study_idx")
    private Long studyIdx;

    @Column(name = "user_idx")
    private Long userIdx;
}
