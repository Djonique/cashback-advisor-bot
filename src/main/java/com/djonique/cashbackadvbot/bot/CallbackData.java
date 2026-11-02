package com.djonique.cashbackadvbot.bot;

public final class CallbackData {

    public static final String MENU_MAIN = "menu:main";
    public static final String MENU_CHOOSE = "menu:choose";
    public static final String MENU_SETUP = "menu:setup";
    public static final String MENU_SUPPORT = "menu:support";

    public static final String SETUP_BANK_PREFIX = "setup:bank:";
    public static final String SETUP_TOGGLE_PREFIX = "setup:toggle:";
    public static final String SETUP_DONE = "setup:done";

    public static final String CARD_BY_CATEGORY = "card:by_category";
    public static final String CARD_BY_MCC = "card:by_mcc";
    public static final String CARD_BY_STORE = "card:by_store";

    private CallbackData() {
    }
}
