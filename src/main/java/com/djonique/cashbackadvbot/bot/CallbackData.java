package com.djonique.cashbackadvbot.bot;

public final class CallbackData {

    public static final String MENU_MAIN = "menu:main";
    public static final String MENU_CHOOSE = "menu:choose";
    public static final String MENU_SETUP = "menu:setup";
    public static final String MENU_SUPPORT = "menu:support";
    public static final String MENU_HELP = "menu:help";

    public static final String SUPPORT_100 = "support:100";
    public static final String SUPPORT_250 = "support:250";
    public static final String SUPPORT_500 = "support:500";

    public static final String HELP_GENERAL = "help:general";
    public static final String HELP_SETUP = "help:setup";
    public static final String HELP_STORE = "help:store";
    public static final String HELP_CATEGORY = "help:category";
    public static final String HELP_MCC = "help:mcc";

    public static final String SETUP_BANK_PREFIX = "setup:bank:";
    public static final String SETUP_TOGGLE_PREFIX = "setup:toggle:";
    public static final String SETUP_DONE = "setup:done";

    public static final String CARD_MENU = "card:menu";
    public static final String CARD_BY_CATEGORY = "card:by_category";
    public static final String CARD_BY_MCC = "card:by_mcc";
    public static final String CARD_BY_STORE = "card:by_store";

    private CallbackData() {
    }
}
