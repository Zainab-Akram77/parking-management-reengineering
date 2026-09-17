package util;

/**
 * Centralized constants for the E-Parking Expert System.
 *
 * Refactoring applied: Replace Magic Number with Symbolic Constant.
 * Before: literals like 30, 20, "1234" were scattered across many files.
 * After: every file references these named constants, so a price or
 * credential change only needs to happen in ONE place.
 */
public final class AppConstants {

    private AppConstants() {
        // utility class - prevent instantiation
    }

    // ---- Parking charges ----
    public static final int CAR_PARKING_CHARGE = 30;
    public static final int BIKE_PARKING_CHARGE = 20;

    // ---- Table name constants (replaces raw "Car"/"Bike" strings) ----
    public static final String CAR_TABLE = "Car";
    public static final String BIKE_TABLE = "Bike";

    // ---- Login credentials ----
    // NOTE: still simple hardcoded values to keep behavior identical to the
    // original system (no new login database table was introduced).
    // Refactoring applied here is only "Extract to named constant" so the
    // values are no longer magic literals buried inside if-statements.
    public static final String STAFF_PASSWORD = "1234";
    public static final String STAFF_CHECKIN_USER = "checkin";
    public static final String STAFF_CHECKOUT_USER = "checkout";

    public static final String ADMIN_PASSWORD = "1234";
    public static final String ADMIN_USER = "admin";
}
