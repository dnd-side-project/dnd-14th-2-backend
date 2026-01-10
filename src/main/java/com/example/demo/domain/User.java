package com.example.demo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nickname;
    private String email;
    private String profile;
    private Provider provider;
    private String providerId;

    public User(String email, String profile, Provider provider, String providerId) {
        this.email = email;
        this.profile = profile;
        this.provider = provider;
        this.providerId = providerId;
    }

    public Long getId() {
        return id;
    }
}
