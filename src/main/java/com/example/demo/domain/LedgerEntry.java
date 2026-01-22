package com.example.demo.domain;

import com.example.demo.domain.enums.LedgerCategory;
import com.example.demo.domain.enums.LedgerType;
import com.example.demo.domain.enums.PaymentMethod;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
public class LedgerEntry {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long amount;            // 9,223,372,036,854,775,807원까지 가능

    @Enumerated(EnumType.STRING)
    private LedgerType type;

    @Enumerated(EnumType.STRING)
    private LedgerCategory category;

    @Size(max = 15)
    @Column(length = 15)
    private String description;

    private LocalDate occurredOn;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Size(max = 100)
    @Column(length = 100)
    private String memo;
}
