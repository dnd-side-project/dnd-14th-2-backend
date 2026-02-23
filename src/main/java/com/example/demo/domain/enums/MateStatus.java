package com.example.demo.domain.enums;

public enum MateStatus {
    PENDING, ACCEPTED, REJECTED;

    public boolean isPending() {
        return this == PENDING;
    }
}
