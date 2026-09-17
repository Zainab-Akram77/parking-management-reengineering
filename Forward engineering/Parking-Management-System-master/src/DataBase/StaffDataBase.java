package DataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all database access for staff accounts.
 *
 * This replaces the old system of two permanently hardcoded logins
 * ("checkin" / "checkout", both with password "1234") with real
 * accounts stored in the database:
 *   - Staff can create their own account (username + password + role)
 *     from the login screen.
 *   - Admin can deactivate an account, reactivate it, or reset a
 *     staff member's password from the new Staff Management screen.
 *
 * Table created (if it does not already exist) the first time this
 * class is used:
 *
 *   CREATE TABLE Staff (
 *       Username VARCHAR(50) PRIMARY KEY,
 *       Password VARCHAR(50),
 *       Role     VARCHAR(20),   -- "checkin", "checkout", or "both"
 *       Active   VARCHAR(1)     -- "Y" or "N"
 *   )
 *
 * Note: passwords are stored as plain text here, matching how the
 * rest of this project already handles the admin/staff passwords
 * (AppConstants.STAFF_PASSWORD / ADMIN_PASSWORD). For a real
 * production system these should be hashed, but that is a separate,
 * larger change from the bug that was reported.
 */
public class StaffDataBase {

    /**
     * Creates the Staff table the first time the app needs it.
     * Safe to call every time the app starts: if the table already
     * exists, the resulting exception is expected and simply ignored.
     */
    public void ensureStaffTable() {
        String sql = "CREATE TABLE Staff ("
                + "Username VARCHAR(50) PRIMARY KEY,"
                + "Password VARCHAR(50),"
                + "Role VARCHAR(20),"
                + "Active VARCHAR(1))";

        try (Connection con = ParkingDataBase.setConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate(sql);
            System.out.println("Staff table created.");
        } catch (Exception e) {
            // Expected on every run after the first one, since the
            // table already exists by then. Only worth printing if
            // the message doesn't look like a "table already exists"
            // style error, to avoid hiding a real connection problem.
            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            if (!msg.contains("already exists") && !msg.contains("duplicate")) {
                System.out.println("Note: could not create Staff table: " + e.getMessage());
            }
        }
    }

    /** Returns true if a staff account with this username already exists. */
    public boolean usernameExists(String username) {
        String sql = "SELECT Username FROM Staff WHERE Username = ?";
        try (Connection con = ParkingDataBase.setConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            System.out.println("Error checking username: " + e.getMessage());
            return false;
        }
    }

    /**
     * Creates a new staff account, active by default.
     * @return true if created, false if the username was already taken
     *         or the insert failed.
     */
    public boolean addStaff(String username, String password, String role) {
        if (usernameExists(username)) {
            return false;
        }
        String sql = "INSERT INTO Staff (Username, Password, Role, Active) VALUES (?, ?, ?, 'Y')";
        try (Connection con = ParkingDataBase.setConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println("Error creating staff account: " + e.getMessage());
            return false;
        }
    }

    /**
     * Validates a login attempt.
     * @return the staff member's role ("checkin"/"checkout"/"both") if
     *         the username/password match and the account is active,
     *         or null if the login should be rejected.
     */
    public String validateLogin(String username, String password) {
        String sql = "SELECT Password, Role, Active FROM Staff WHERE Username = ?";
        try (Connection con = ParkingDataBase.setConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("Password");
                    String active = rs.getString("Active");
                    if (!"Y".equalsIgnoreCase(active)) {
                        return null; // account deactivated by admin
                    }
                    if (storedPassword != null && storedPassword.equals(password)) {
                        return rs.getString("Role");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error validating login: " + e.getMessage());
        }
        return null;
    }

    /** Activates or deactivates a staff account. */
    public void setActive(String username, boolean active) {
        String sql = "UPDATE Staff SET Active = ? WHERE Username = ?";
        try (Connection con = ParkingDataBase.setConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, active ? "Y" : "N");
            ps.setString(2, username);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error updating staff status: " + e.getMessage());
        }
    }

    /** Lets the admin reset a staff member's password. */
    public void updatePassword(String username, String newPassword) {
        String sql = "UPDATE Staff SET Password = ? WHERE Username = ?";
        try (Connection con = ParkingDataBase.setConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setString(2, username);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error updating staff password: " + e.getMessage());
        }
    }

    /** Permanently removes a staff account. */
    public void deleteStaff(String username) {
        String sql = "DELETE FROM Staff WHERE Username = ?";
        try (Connection con = ParkingDataBase.setConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error deleting staff account: " + e.getMessage());
        }
    }

    /**
     * Lists every staff account for the admin's Staff Management table.
     * @return rows of {Username, Role, Active}
     */
    public List<Object[]> listStaff() {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT Username, Role, Active FROM Staff";
        try (Connection con = ParkingDataBase.setConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new Object[]{
                    rs.getString("Username"),
                    rs.getString("Role"),
                    "Y".equalsIgnoreCase(rs.getString("Active")) ? "Active" : "Deactivated"
                });
            }
        } catch (Exception e) {
            System.out.println("Error listing staff accounts: " + e.getMessage());
        }
        return rows;
    }
}
