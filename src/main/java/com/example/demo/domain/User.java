package com.example.demo.domain;

import com.example.demo.domain.enums.UserType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(
        name = "uk_user_provider_provider_id",
        columnNames = {"provider", "provider_id"}
    ),
})
@NoArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "nickname", unique = true))
    private Nickname nickname;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "invitation_code", unique = true, length = 6))
    private InvitationCode invitationCode;

    @Column(nullable = false)
    private String email;

    @Column(length = 2048)
    private String profile;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    @Column(nullable = false)
    private String providerId;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private UserType userType;

    private Integer level = 0;

    public User(Nickname nickname, InvitationCode invitationCode, String email, String profile, Provider provider, String providerId, UserType userType) {
        this.nickname = nickname;
        this.invitationCode = invitationCode;
        this.email = email;
        this.profile = profile;
        this.provider = provider;
        this.providerId = providerId;
        this.userType = userType;
    }

    public void changeNickname(Nickname nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname.value();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return getId() != null && getId().equals(user.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
