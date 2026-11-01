package com.djonique.cashbackadvbot.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bank_category_mcc")
@Getter
@Setter
@NoArgsConstructor
public class BankCategoryMcc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bank_category_id", nullable = false)
    private BankCategory bankCategory;

    @Column(name = "mcc_code", nullable = false, length = 4)
    private String mccCode;
}
