package com.example.demo.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    @AttributeOverride(name = "value", column = @Column(name = "nickname", unique = true, length = 5))
    private Nickname nickname;

    @Column(nullable = false)
    private String email;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "invitation_code", unique = true, length = 6))
    private InvitationCode invitationCode;

    @Column(length = 2048, nullable = false)
    private String profile;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    @Column(nullable = false)
    private String providerId;

    private Integer level = 0;

    private LocalDateTime deletedAt;

    public User(String email, Nickname nickname, InvitationCode invitationCode, String profile, Provider provider, String providerId) {
        this.email = email;
        this.nickname = nickname;
        this.invitationCode = invitationCode;
        this.profile = profile;
        this.provider = provider;
        this.providerId = providerId;
    }

    public void withdraw(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
        this.email = null;
        this.profile = null;
    }

    public void changeNickname(String nickname) {
        this.nickname = new Nickname(nickname);
    }

    public String getNickname() {
        return nickname.value();
    }
}
