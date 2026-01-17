package com.example.demo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String token;

    protected RefreshToken() {
    }

    public RefreshToken(Long userId, String token) {
        this.userId = userId;
        this.token = token;
    }

    public void rotate(String refreshToken) {
        this.token = refreshToken;
    }
}