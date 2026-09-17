package util;

import javax.swing.JFrame;

/**
 * Shared window-closing helper.
 *
 * Refactoring applied: Extract Method + Move Method (Pull Up).
 * Before: an identical private SystemExit() method was copy-pasted into
 * login.java, display.java, MainPage.java, Adlog.java and AdminPanel.java
 * (5 separate copies of the same 3 lines of code).
 * After: one shared static method that any JFrame can call.
 */
public final class WindowUtils {

    private WindowUtils() {
    }

    /**
     * Closes/disposes the given frame. Replaces the duplicated
     * SystemExit() method that existed in 5 different classes.
     */
    public static void closeWindow(JFrame frame) {
        frame.dispose();
    }
}
