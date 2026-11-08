package com.djonique.cashbackadvbot.bot;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum FixedDonation {

    SERVER(100, "🖥 На сервер (100 ₽)", CallbackData.SUPPORT_100),
    DEPOSIT(250, "💵 На додеп (250 ₽)", CallbackData.SUPPORT_250),
    CATS(500, "😻 На котят (500 ₽)", CallbackData.SUPPORT_500);

    private final int amountRub;
    private final String label;
    private final String callbackData;

    FixedDonation(int amountRub, String label, String callbackData) {
        this.amountRub = amountRub;
        this.label = label;
        this.callbackData = callbackData;
    }

    public static Optional<FixedDonation> fromCallback(String callbackData) {
        return Arrays.stream(values())
                .filter(donation -> donation.callbackData.equals(callbackData))
                .findFirst();
    }
}
