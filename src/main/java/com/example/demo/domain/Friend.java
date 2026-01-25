package com.example.demo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"member_a_id", "member_b_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_a_id", nullable = false)
    private User memberA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_b_id", nullable = false)
    private User memberB;

    public Friend(User memberA, User memberB) {
        if (memberA.getId().equals(memberB.getId())) {
            throw new IllegalArgumentException("자기 자신을 팔로우 할 수 없습니다.");
        }

        if (memberA.getId() < memberB.getId()) {
            this.memberA = memberA;
            this.memberB = memberB;
        } else {
            this.memberA = memberB;
            this.memberB = memberA;
        }
    }
}
