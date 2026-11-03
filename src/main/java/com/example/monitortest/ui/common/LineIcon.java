package com.example.monitortest.ui.common;


import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public  class LineIcon implements Icon {
    private final String name;
    private final int size;
    private Color color;
    private float strokeWidth = 2f;   // in 24x24 grid units

    public LineIcon(String name, int size, Color color) {
        this.name = name;
        this.size = size;
        this.color = color;
    }

    /** Returns this icon with a different line thickness (e.g. thicker for tiny ticks). */
    public LineIcon withStroke(float width) {
        this.strokeWidth = width;
        return this;
    }

    public void setColor(Color color) { this.color = color; }

    public int getIconWidth() { return size; }
    public int getIconHeight() { return size; }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        // Create a smooth graphics context for drawing the vector icon.
        Graphics2D g2 = GraphicsUtils.createSmoothTextGraphics(g);
        g2.translate(x, y);
        g2.scale(size / 24.0, size / 24.0);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        draw(g2);
        g2.dispose();
    }

    public void draw(Graphics2D g) {
        switch (name) {
            case "LEAF" -> {
                Path2D leaf = new Path2D.Double();
                leaf.moveTo(11, 20);
                leaf.curveTo(6.5, 20.5, 3, 15.5, 4.8, 11);
                leaf.curveTo(5.8, 8.4, 7.6, 6.6, 9.8, 6.1);
                leaf.curveTo(15.5, 5, 17, 4.48, 19, 2);
                leaf.curveTo(20, 4, 21, 6.18, 21, 10);
                leaf.curveTo(21, 15.5, 16.22, 20, 11, 20);
                leaf.closePath();
                g.draw(leaf);
                Path2D stem = new Path2D.Double();
                stem.moveTo(2, 21);
                stem.curveTo(2, 18, 3.85, 15.64, 7.08, 15);
                stem.curveTo(9.5, 14.52, 12, 13, 13, 12);
                g.draw(stem);
            }
            case "HOME" -> polyline(g, true, 3, 10.5, 12, 3, 21, 10.5, 21, 21, 15, 21, 15, 15, 9, 15, 9, 21, 3, 21);
            case "RESULTS" -> {
                Path2D page = new Path2D.Double();
                page.moveTo(14, 2);
                page.lineTo(6, 2);
                page.quadTo(4, 2, 4, 4);
                page.lineTo(4, 20);
                page.quadTo(4, 22, 6, 22);
                page.lineTo(18, 22);
                page.quadTo(20, 22, 20, 20);
                page.lineTo(20, 8);
                page.closePath();
                g.draw(page);
                polyline(g, false, 14, 2, 14, 8, 20, 8);
                line(g, 8, 13, 16, 13);
                line(g, 8, 17, 16, 17);
                line(g, 8, 9, 10, 9);
            }
            case "HISTORY" -> {
                g.draw(new Arc2D.Double(3, 3, 18, 18, 165, 315, Arc2D.OPEN));
                polyline(g, false, 3, 3, 3, 8, 8, 8);
                polyline(g, false, 12, 7, 12, 12, 16, 14);
            }
            case "SCAN" -> {
                corner(g, 3, 7, 3, 3, 7, 3);
                corner(g, 17, 3, 21, 3, 21, 7);
                corner(g, 21, 17, 21, 21, 17, 21);
                corner(g, 7, 21, 3, 21, 3, 17);
                g.draw(new Ellipse2D.Double(9, 9, 6, 6));
                dot(g, 12, 12);
            }
            case "BRIEFCASE" -> {
                g.draw(new RoundRectangle2D.Double(2, 7, 20, 14, 4, 4));
                Path2D handle = new Path2D.Double();
                handle.moveTo(8, 7);
                handle.lineTo(8, 5);
                handle.quadTo(8, 3, 10, 3);
                handle.lineTo(14, 3);
                handle.quadTo(16, 3, 16, 5);
                handle.lineTo(16, 7);
                g.draw(handle);
                line(g, 2, 13, 22, 13);
            }
            case "GAMEPAD" -> {
                g.draw(new RoundRectangle2D.Double(2, 7, 20, 11, 9, 9));
                line(g, 6, 12.5, 10, 12.5);
                line(g, 8, 10.5, 8, 14.5);
                dot(g, 15, 11.5);
                dot(g, 18, 13.5);
            }
            case "PENCIL" -> {
                polyline(g, true, 16, 3.5, 20.5, 8, 7.5, 21, 2.5, 21.5, 3, 16.5);
                line(g, 13.5, 6, 18, 10.5);
            }
            case "SHIELD" -> {
                Path2D shield = new Path2D.Double();
                shield.moveTo(12, 22);
                shield.curveTo(12, 22, 20, 18, 20, 12);
                shield.lineTo(20, 5);
                shield.lineTo(12, 2);
                shield.lineTo(4, 5);
                shield.lineTo(4, 12);
                shield.curveTo(4, 18, 12, 22, 12, 22);
                shield.closePath();
                g.draw(shield);
                polyline(g, false, 9, 12, 11, 14, 15, 10);
            }
            case "ARROW_RIGHT" -> {
                line(g, 5, 12, 19, 12);
                polyline(g, false, 12, 5, 19, 12, 12, 19);
            }
            case "MONITOR" -> {
                g.draw(new RoundRectangle2D.Double(2, 3, 20, 14, 4, 4));
                line(g, 8, 21, 16, 21);
                line(g, 12, 17, 12, 21);
            }
            case "CPU" -> {
                g.draw(new RoundRectangle2D.Double(4, 4, 16, 16, 4, 4));
                g.draw(new Rectangle2D.Double(9, 9, 6, 6));
                for (double p : new double[]{9, 15}) {
                    line(g, p, 1, p, 4);
                    line(g, p, 20, p, 23);
                    line(g, 1, p, 4, p);
                    line(g, 20, p, 23, p);
                }
            }
            case "RAM" -> {
                g.draw(new RoundRectangle2D.Double(2, 6, 20, 12, 4, 4));
                for (double x = 6; x <= 18; x += 4) line(g, x, 10, x, 14);
                line(g, 6, 18, 6, 21);
                line(g, 18, 18, 18, 21);
            }
            case "GPU" -> {
                Path2D card = new Path2D.Double();
                card.moveTo(2, 6);
                card.lineTo(19, 6);
                card.quadTo(21, 6, 21, 8);
                card.lineTo(21, 16);
                card.quadTo(21, 18, 19, 18);
                card.lineTo(2, 18);
                g.draw(card);
                line(g, 2, 4, 2, 21);
                g.draw(new Ellipse2D.Double(10, 9.5, 5, 5));
                line(g, 6, 10, 6, 14);
            }
            case "STORAGE" -> {
                g.draw(new Ellipse2D.Double(3, 2, 18, 6));
                line(g, 3, 5, 3, 19);
                line(g, 21, 5, 21, 19);
                g.draw(new Arc2D.Double(3, 9, 18, 6, 180, 180, Arc2D.OPEN));
                g.draw(new Arc2D.Double(3, 16, 18, 6, 180, 180, Arc2D.OPEN));
            }
            case "OS" -> {
                g.draw(new RoundRectangle2D.Double(3, 3, 7, 7, 2, 2));
                g.draw(new RoundRectangle2D.Double(14, 3, 7, 7, 2, 2));
                g.draw(new RoundRectangle2D.Double(14, 14, 7, 7, 2, 2));
                g.draw(new RoundRectangle2D.Double(3, 14, 7, 7, 2, 2));
            }
            case "CHECK" -> polyline(g, false, 20, 6, 9, 17, 4, 12);
            case "RESTART" -> {
                g.draw(new Arc2D.Double(3, 3, 18, 18, 45, 315, Arc2D.OPEN));
                polyline(g, false, 21, 3, 21, 8, 16, 8);
            }
            case "WARNING" -> {
                Path2D triangle = new Path2D.Double();
                triangle.moveTo(10.3, 3.9);
                triangle.lineTo(1.8, 18);
                triangle.quadTo(1, 21, 3.5, 21);
                triangle.lineTo(20.5, 21);
                triangle.quadTo(23, 21, 22.2, 18);
                triangle.lineTo(13.7, 3.9);
                triangle.quadTo(12, 1.4, 10.3, 3.9);
                triangle.closePath();
                g.draw(triangle);
                line(g, 12, 9, 12, 13);
                dot(g, 12, 17);
            }
            default -> throw new IllegalArgumentException("Unknown icon: " + name);
        }
    }

    // ---- Small drawing helpers ----
    public static void line(Graphics2D g, double x1, double y1, double x2, double y2) {
        g.draw(new Line2D.Double(x1, y1, x2, y2));
    }

    /** A dot: a tiny line that the round line cap turns into a circle. */
    public static void dot(Graphics2D g, double x, double y) {
        g.draw(new Line2D.Double(x, y, x + 0.01, y));
    }

    /** Connects the given x,y pairs with straight lines; closed = join the last point to the first. */
    private static void polyline(Graphics2D g, boolean closed, double... xy) {
        Path2D p = new Path2D.Double();
        p.moveTo(xy[0], xy[1]);
        for (int i = 2; i < xy.length; i += 2) p.lineTo(xy[i], xy[i + 1]);
        if (closed) p.closePath();
        g.draw(p);
    }

    /** A rounded corner bracket for the scan icon: start -> corner -> end. */
    private static void corner(Graphics2D g, double x1, double y1, double cx, double cy, double x2, double y2) {
        Path2D p = new Path2D.Double();
        p.moveTo(x1, y1);
        p.lineTo(cx + (x1 - cx) / 2, cy + (y1 - cy) / 2);
        p.quadTo(cx, cy, cx + (x2 - cx) / 2, cy + (y2 - cy) / 2);
        p.lineTo(x2, y2);
        g.draw(p);
    }
}

