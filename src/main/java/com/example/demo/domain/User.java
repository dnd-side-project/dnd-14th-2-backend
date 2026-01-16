package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class User {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname;

    private String email;

    @Column(length = 2048)
    private String profile;

    @Enumerated(value = EnumType.STRING)
    private Provider provider;

    private String providerId;

    public User(String email, String profile, Provider provider, String providerId) {
        this.email = email;
        this.profile = profile;
        this.provider = provider;
        this.providerId = providerId;
    }

}
