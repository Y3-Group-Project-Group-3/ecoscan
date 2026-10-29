package com.example.monitortest.ui.common;

import javax.swing.*;
import java.awt.*;

/** Panel that fills the scroll pane's visible area, so backgrounds reach the bottom of the window. */
public class ScrollablePanel extends JPanel implements Scrollable {

    protected ScrollablePanel(LayoutManager layout) { super(layout); }

    public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
    public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 160; }
    public boolean getScrollableTracksViewportWidth() { return true; }
    public boolean getScrollableTracksViewportHeight() {
        // Stretch to the window height unless the content is taller (then scroll)
        return getParent() instanceof JViewport v && v.getHeight() > getPreferredSize().height;
    }
}