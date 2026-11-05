package com.djonique.cashbackadvbot.bot.state;

public enum DialogState {
    IDLE,
    CHOOSING_BANK,
    CHOOSING_CATEGORIES,
    ENTERING_PERCENT,
    CARD_INPUT_MCC,
    CARD_INPUT_CATEGORY,
    CARD_INPUT_STORE
}
