package com.ieumsae.project.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_interest_part")
@Getter
@Setter
public class UserInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_idx")
    private Long userIdx;

    @Column(name = "user_sub_1")
    private String userInterest1; // 관심분야1

    @Column(name = "user_sub_2")
    private String userInterest2; // 관심분야2

    @Column(name = "user_region_1")
    private String userRegion1; // 관심지역1

    @Column(name = "user_region_2")
    private String userRegion2; // 관심지역2

    // Getter, Setter methods

}