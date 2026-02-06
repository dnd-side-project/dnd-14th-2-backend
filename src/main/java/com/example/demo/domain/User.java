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

    private String nickname;

    private String email;

    @Column(length = 2048)
    private String profile;

    @Enumerated(value = EnumType.STRING)
    private Provider provider;

    private String providerId;

    private Integer level = 0;

    private LocalDateTime deletedAt;

    public User(String email, String profile, Provider provider, String providerId) {
        this.email = email;
        this.profile = profile;
        this.provider = provider;
        this.providerId = providerId;
    }

    public void withdraw(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
        this.email = null;
        this.profile = null;
    }
}
