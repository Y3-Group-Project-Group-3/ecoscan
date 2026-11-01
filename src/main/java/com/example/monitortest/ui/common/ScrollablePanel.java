package com.example.monitortest.ui.common;

import javax.swing.*;
import java.awt.*;

// custom JPanel that fills the available viewport space while allowing vertical scrolling when content exceeds it.
public class ScrollablePanel extends JPanel implements Scrollable {

    // Creates a scrollable panel using the supplied layout manager.
    public ScrollablePanel(LayoutManager layout) {
        super(layout);
    }

    // Returns the panel's preferred size within a JScrollPane.
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    // Defines the scrolling distance for a single increment.
    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 16;
    }

    // Defines the scrolling distance for a larger increment.
    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 160;
    }

    // Matches the viewport width to prevent horizontal scrolling.
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    // Expands to fill the viewport height unless vertical scrolling is needed.
    @Override
    public boolean getScrollableTracksViewportHeight() {
        // Check whether the viewport is taller than the panel's preferred height.
        return getParent() instanceof JViewport viewport && viewport.getHeight() > getPreferredSize().height;
    }
}
