package com.example.demo.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(
        name = "uk_user_provider_provider_id",
        columnNames = {"provider", "provider_id"}
    )
})
@NoArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public class User extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "nickname"))
    private Nickname nickname;

    @Column(nullable = false)
    private String email;

    @Column(length = 2048, nullable = false)
    private String profile;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    @Column(nullable = false)
    private String providerId;

    private Integer level = 0;

    private LocalDateTime deletedAt;

    public User(String email, String profile, Provider provider, String providerId) {
        this.email = email;
        this.profile = profile;
        this.provider = provider;
        this.providerId = providerId;
    }

    public void registerNickname(String nickname) {
        if (this.nickname != null) {
            throw new IllegalArgumentException("이미 닉네임이 등록된 사용자입니다.");
        }

        this.nickname = new Nickname(nickname);
    }
    public void withdraw(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
        this.email = null;
        this.profile = null;
    }
}
