package com.example.monitortest.ui.common;

import javax.swing.*;
import java.awt.*;

// panel that stretches to the window width (stops sideways scrolling)
public class FitWidthPanel extends JPanel implements Scrollable {
    public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
    public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 200; }
    public boolean getScrollableTracksViewportWidth() { return true; }
    public boolean getScrollableTracksViewportHeight() { return false; }
}