package com.djonique.cashbackadvbot.repository;

import com.djonique.cashbackadvbot.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Modifying
    @Query(value = """
            INSERT INTO payment (
                user_id,
                telegram_payment_charge_id,
                provider_payment_charge_id,
                invoice_payload,
                amount,
                currency,
                created_at
            ) VALUES (
                :userId,
                :telegramChargeId,
                :providerChargeId,
                :invoicePayload,
                :amount,
                :currency,
                now()
            )
            ON CONFLICT (telegram_payment_charge_id) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(@Param("userId") Long userId,
                       @Param("telegramChargeId") String telegramChargeId,
                       @Param("providerChargeId") String providerChargeId,
                       @Param("invoicePayload") String invoicePayload,
                       @Param("amount") long amount,
                       @Param("currency") String currency);
}
