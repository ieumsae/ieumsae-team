package com.ieumsae.chat.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomId;

    @Enumerated(EnumType.STRING)
    private RoomType roomType;

    @ElementCollection
    @CollectionTable(name = "", joinColumns = @JoinColumn(name = ""))
    @MapKeyColumn(name = "")
    @Column(name = "")
    private Map<String, LocalDateTime> userJoinTimes = new HashMap<>();

    public enum RoomType {
        PERSONAL, GROUP
    }
}