package com.example.monitortest.ui.history.components;

import com.example.monitortest.ui.common.OutlinedActionButton;

import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import java.awt.*;

import static com.example.monitortest.ui.theme.AppTheme.*;

// Reusable destructive action button for History section headers.
// Both Clear Scans and Clear Sessions share this appearance.
public class HistoryClearButton extends OutlinedActionButton {

    // Standard colours used when hovering over a destructive action.
    private static final Color DELETE_HOVER_BG = new Color(0xFDEEEE);
    private static final Color DELETE_HOVER_BORDER = new Color(0xC0392B);

    // Creates a styled clearing button with a supplied click action.
    public HistoryClearButton(String text, Runnable clearAction) {
        super(text);

        // Use the same muted trash icon across History sections.
        setIcon( FontIcon.of(FontAwesomeSolid.TRASH_ALT, 13, TEXT_MUTED) );
        setDisabledIcon( FontIcon.of(FontAwesomeSolid.TRASH_ALT, 13, TEXT_MUTED));
        setIconTextGap(8);

        // Apply the standard destructive hover appearance.
        setHoverColors(DELETE_HOVER_BG, DELETE_HOVER_BORDER);

        // Keep section clearing buttons identical in size.
        setPreferredSize(new Dimension(145, 36));

        // Delegate the actual operation to the owning History section.
        addActionListener(e -> clearAction.run());
    }
}
