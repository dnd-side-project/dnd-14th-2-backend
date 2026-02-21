package com.example.demo.domain;

import com.example.demo.domain.enums.MateStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Mate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MateStatus status;

    private LocalDateTime acceptedAt;

    public Mate(User requester, User receiver) {
        if (requester == null) {
            throw new IllegalArgumentException("요청자는 필수입니다.");
        }
        if (receiver == null) {
            throw new IllegalArgumentException("수신자는 필수입니다.");
        }
        if (requester.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("자기 자신에게 친구 요청을 보낼 수 없습니다.");
        }
        this.requester = requester;
        this.receiver = receiver;
        this.status = MateStatus.PENDING;
    }

    public void accept() {
        if (this.status != MateStatus.PENDING) {
            throw new IllegalArgumentException("대기 중인 요청만 수락할 수 있습니다.");
        }
        this.status = MateStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
    }

    public void reject() {
        if (this.status != MateStatus.PENDING) {
            throw new IllegalArgumentException("대기 중인 요청만 거절할 수 있습니다.");
        }
        this.status = MateStatus.REJECTED;
    }

    public User getFriend(Long myUserId) {
        if (this.requester.getId().equals(myUserId)) {
            return this.receiver;
        }
        return this.requester;
    }
}
