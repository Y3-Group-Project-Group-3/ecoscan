package com.example.monitortest.ui.theme;

import java.awt.*;

public class AppTheme {

    private AppTheme() {
        // Prevent creating instances of this.
    }

    public static final Color BG_COLOR = new Color(0xF4F6F9);          // page background
    public static final Color SIDEBAR_BG = Color.WHITE;
    public static final Color SIDEBAR_BORDER = new Color(0xE7EBF0);
    public static final Color SIDEBAR_SELECTED = new Color(0xEAF1FD);  // active nav item
    public static final Color SIDEBAR_HOVER = new Color(0xF3F6FA);
    public static final Color DIVIDER = new Color(0xEEF1F5);

    public static final Color GREEN = new Color(0x16834D);             // main brand green
    public static final Color GREEN_LIGHT = new Color(0x2F9A5F);
    public static final Color BUTTON_GREEN = new Color(0x3B8B5A);      // Scan Computer button
    public static final Color BUTTON_GREEN_HOVER = new Color(0x2F7A4C);

    public static final Color TEXT_MAIN = new Color(0x0F172A);         // headings
    public static final Color TEXT_BODY = new Color(0x475569);         // normal text
    public static final Color TEXT_MUTED = new Color(0x64748B);        // descriptions
    public static final Color TEXT_FAINT = new Color(0x94A3B8);        // footnotes, "WAITING"
    public static final Color BORDER_COLOR = new Color(0xE3E8EF);

//    private static final Color BG_COLOR = new Color(0xF4F6F8);
    public static final Color CARD_BG = Color.WHITE;
//    private static final Color BORDER_COLOR = new Color(0xE3E7EB);
//    private static final Color GREEN = new Color(0x2E7D4F);
//    private static final Color GREEN_LIGHT = new Color(0xEAF5EE);
//    private static final Color TEXT_MAIN = new Color(0x111827);
//    private static final Color TEXT_MUTED = new Color(0x6B7280);
    public static final Color TRACK = new Color(0xE5E7EB);
    public static final Color GREY_BAR = new Color(0xA3ABB8);
    public static final String FONT_FAMILY = "Segoe UI";

    // ---- Font sizes ----
    // Every text size in the app comes from this list, so the type stays consistent across screens.
    // Sizes are set for readability: body text is 15 and nothing is smaller than 12,
    // so EcoScan stays usable for people with weaker eyesight.
    // To make text bigger or smaller everywhere, change the number here instead of in each panel.
    public static final float FONT_DISPLAY_SIZE     = 44f; // the large recommendation word (e.g. "KEEP")
    public static final float FONT_SCORE_SIZE       = 36f; // the number inside the eco score ring
    public static final float FONT_TITLE_SIZE       = 30f; // page titles (Home, Loading, Results, History)
    public static final float FONT_STAT_SIZE        = 28f; // large figures in the impact cards
    public static final float FONT_HEADING_SIZE     = 22f; // section headings and the loading percentage
    public static final float FONT_SUBHEADING_SIZE  = 18f; // app name in the navigation bar
    public static final float FONT_CARD_TITLE_SIZE = 16f; // component names and card totals
    public static final float FONT_BODY_SIZE = 15f; // normal text, buttons and navigation items
    public static final float FONT_LABEL_SIZE = 14f; // section labels, units and small controls
    public static final float FONT_SMALL_SIZE = 13f; // captions, hints and secondary details
    public static final float FONT_BADGE_SIZE = 12f; // badges and the smallest labels



}
