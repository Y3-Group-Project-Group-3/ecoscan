package com.example.monitortest.testsupport;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BooleanSupplier;

/**
 * Shared helpers for EcoScan's Swing tests.
 *
 * The tests run headless, so nothing is ever shown on screen. Components are
 * created and inspected on the Event Dispatch Thread (EDT), and the component
 * tree is searched instead of reaching into private fields.
 */
public final class SwingTestSupport {

    private SwingTestSupport() {
    }

    /** Forces headless mode. Call this before any AWT or Swing class is used. */
    public static void useHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }


    // ---- Threading ----

    /** Runs the task on the EDT and returns its result. */
    public static <T> T onEdt(Callable<T> task) {
        if (SwingUtilities.isEventDispatchThread()) {
            return call(task);
        }

        Object[] result = new Object[1];
        Throwable[] error = new Throwable[1];

        try {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    result[0] = task.call();
                } catch (Throwable t) {
                    error[0] = t;
                }
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e.getCause());
        }

        if (error[0] != null) {
            throw new IllegalStateException(error[0]);
        }

        @SuppressWarnings("unchecked")
        T value = (T) result[0];
        return value;
    }

    /** Runs the task on the EDT. */
    public static void onEdt(Runnable task) {
        onEdt(() -> {
            task.run();
            return null;
        });
    }

    /** Waits until everything already queued on the EDT has been processed. */
    public static void flushEdt() {
        // Two passes so events posted by earlier events are processed too.
        onEdt(() -> { });
        onEdt(() -> { });
    }

    /** Polls until the condition is true or the timeout passes. Returns the last result. */
    public static boolean waitUntil(BooleanSupplier condition, long timeoutMillis) {
        long deadline = System.currentTimeMillis() + timeoutMillis;

        while (System.currentTimeMillis() < deadline) {
            if (onEdt(() -> condition.getAsBoolean())) {
                return true;
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return onEdt(() -> condition.getAsBoolean());
    }

    private static <T> T call(Callable<T> task) {
        try {
            return task.call();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }


    // ---- Component tree searching ----

    /** Every component of the given type below the root, in depth-first order. */
    public static <T extends Component> List<T> findAll(Container root, Class<T> type) {
        List<T> found = new ArrayList<>();
        collect(root, type, found);
        return found;
    }

    private static <T extends Component> void collect(Container parent, Class<T> type, List<T> found) {
        for (Component child : parent.getComponents()) {
            if (type.isInstance(child)) {
                found.add(type.cast(child));
            }
            if (child instanceof Container container) {
                collect(container, type, found);
            }
        }
    }

    /** The label whose text equals the given text, or null if there is none. */
    public static JLabel findLabel(Container root, String text) {
        for (JLabel label : findAll(root, JLabel.class)) {
            if (text.equals(label.getText())) {
                return label;
            }
        }
        return null;
    }

    /** The label whose text contains the given text (HTML labels included), or null. */
    public static JLabel findLabelContaining(Container root, String fragment) {
        for (JLabel label : findAll(root, JLabel.class)) {
            if (label.getText() != null && label.getText().contains(fragment)) {
                return label;
            }
        }
        return null;
    }

    /** How many labels have exactly this text. */
    public static int countLabels(Container root, String text) {
        int count = 0;
        for (JLabel label : findAll(root, JLabel.class)) {
            if (text.equals(label.getText())) {
                count++;
            }
        }
        return count;
    }

    /** The button whose text equals the given text, or null. */
    public static JButton findButton(Container root, String text) {
        for (JButton button : findAll(root, JButton.class)) {
            if (text.equals(button.getText())) {
                return button;
            }
        }
        return null;
    }

    /** True when the component and every ancestor up to the root are visible. */
    public static boolean isVisibleWithin(Component component, Container root) {
        Component current = component;

        while (current != null) {
            if (!current.isVisible()) {
                return false;
            }
            if (current == root) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }


    // ---- Layout and painting without a window ----

    /**
     * Lays out a component tree that has no window.
     * Swing only validates components that are on screen, so this walks the tree by hand.
     */
    public static void layoutTree(Container container) {
        container.doLayout();

        for (Component child : container.getComponents()) {
            if (child instanceof Container inner) {
                layoutTree(inner);
            }
        }
    }

    /** Sizes the component, lays it out and lets queued resize events run. */
    public static void sizeAndLayout(JComponent component, int width, int height) {
        onEdt(() -> {
            component.setSize(width, height);
            layoutTree(component);
        });
        flushEdt();
        // Layout can resize children whose listeners then change sizes again.
        onEdt(() -> layoutTree(component));
        flushEdt();
    }

    // ---- Mouse input ----

    /** Sends a synthetic mouse click to the component on the EDT. */
    public static void click(Component target, int button, int clickCount) {
        onEdt(() -> {
            MouseEvent event = new MouseEvent(
                    target,
                    MouseEvent.MOUSE_CLICKED,
                    System.currentTimeMillis(),
                    0,
                    5, 5,
                    clickCount,
                    false,
                    button
            );
            target.dispatchEvent(event);
        });
    }

    /** A normal single left click. */
    public static void click(Component target) {
        click(target, MouseEvent.BUTTON1, 1);
    }


    /** Paints the component into an image. Transparent pixels stay transparent. */
    public static BufferedImage paintToImage(JComponent component) {
        return onEdt(() -> {
            int width = Math.max(1, component.getWidth());
            int height = Math.max(1, component.getHeight());

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = image.createGraphics();
            try {
                component.paint(g2);
            } finally {
                g2.dispose();
            }
            return image;
        });
    }
}
