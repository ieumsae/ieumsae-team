package com.ieumsae.project.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "grade_point")
public class GradePoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "user_idx")
    private Long userIdx;

    @Column(name = "increase_grade_point")
    private Integer increaseGradePoint;

    @Column(name = "last_grade_pont")
    private Integer lastGradePont;

    @Column(name = "increase_grade_point_dt")
    private Date increaseGradePointDt;
}
