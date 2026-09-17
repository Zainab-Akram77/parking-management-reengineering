package DataBase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;
import util.DbConfig;

/**
 * Handles all database access for parking records.
 *
 * Refactorings applied in this class:
 * 1. Replace Hardcoded Path with Relative Path (uses util.DbConfig).
 * 2. Replace Statement + String Concatenation with PreparedStatement
 *    (removes the SQL Injection vulnerability that existed on every method).
 * 3. Introduce try-with-resources so Connection/Statement/ResultSet are
 *    ALWAYS closed, even when an exception happens (fixes resource leaks
 *    that existed in the original code on every error path).
 * 4. Original method names (InsertCarDetail, GetData, etc.) are kept as
 *    thin wrappers at the bottom so every existing call site elsewhere
 *    in the project keeps working unchanged.
 * 5. BUG FIX: setConnection() failures used to only print to the
 *    console (invisible when running outside the IDE), which made every
 *    downstream method fail with a confusing "con is null" error with
 *    no clue why. Now shows a clear popup with the actual cause (e.g.
 *    missing driver, or .accdb file not found at the resolved path).
 */
public class ParkingDataBase {

    public static Connection setConnection() {
        String url = DbConfig.getDatabaseUrl();
        Connection con = null;
        try {
            con = DriverManager.getConnection(url);
            System.out.println("Connection Established Successfully");
        } catch (Exception sqlEx) {
            String message = "Could not connect to the database.\n\n"
                    + "Resolved URL: " + url + "\n\n"
                    + "Cause: " + sqlEx.getMessage() + "\n\n"
                    + "Check that:\n"
                    + "1. parking.accdb exists at the path shown above.\n"
                    + "2. The UCanAccess JDBC driver jars are added to\n"
                    + "   this project's Libraries in NetBeans.";
            System.out.println(message);
            JOptionPane.showMessageDialog(null, message, "Database Connection Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        return con;
    }

    /**
     * Returns the next available ID for the given table.
     *
     * BUG FIX: this used to return (row count + 1). That is WRONG as a
     * unique-ID generator: it only works if rows are never deleted and
     * the ID field is never edited by hand. As soon as either happens,
     * "row count + 1" can produce an ID that already exists in the
     * table, which is exactly the duplicate-ID bug that was reported
     * (two vehicles saved with the same ID, so looking that ID up
     * afterwards returns/overwrites the wrong record).
     *
     * The correct way to generate a unique next ID is MAX(ID) + 1,
     * because that is guaranteed to be higher than every ID currently
     * stored in the table, regardless of deletions or gaps.
     */
    public int getID(String tableName) {
        String safeTable = sanitizeIdentifier(tableName);
        String sql = "SELECT MAX(ID) AS MaxId FROM " + safeTable;
        // Table names cannot be parameterized in JDBC, so they are
        // restricted to a safe allow-list via sanitizeIdentifier().

        int maxId = 0;
        try (Connection con = setConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                // rs.getInt(...) returns 0 both when the real value is
                // 0 and when it is SQL NULL (empty table) - either way
                // "0 + 1 = 1" is the correct first ID, so no special
                // casing is needed here.
                maxId = rs.getInt("MaxId");
            }
        } catch (Exception e) {
            System.out.println("Error occurred while fetching ID: " + e.getMessage());
        }
        return maxId + 1;
    }

    /**
     * Counts parking records for a table, optionally filtered by date.
     * Used to calculate revenue.
     */
    public static int getRevenue(String tableName, String date) {
        int count = 0;
        String safeTable = sanitizeIdentifier(tableName);

        String sql = date.equals("")
                ? "SELECT * FROM " + safeTable
                : "SELECT * FROM " + safeTable + " WHERE ParkingDate = ?";

        try (Connection con = setConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (!date.equals("")) {
                ps.setString(1, date);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    count++;
                }
            }
        } catch (Exception e) {
            System.out.println("Error occurred while fetching revenue: " + e.getMessage());
        }
        return count;
    }

    /**
     * Inserts a new parking detail record and updates the position table
     * to mark that slot as occupied.
     */
    public void insertParkingDetail(String tableName, int id, String regNo, String position) {
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss a");
        String time = timeFormat.format(new Date());

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String date = dateFormat.format(new Date());

        String safeTable = sanitizeIdentifier(tableName);

        String insertSql = "INSERT INTO " + safeTable
                + "Detail(ID,RegNo,Position,ParkingDate,TimeIn) VALUES(?,?,?,?,?)";
        String updateRegSql = "UPDATE " + safeTable + "Position SET RegNo = ? WHERE Position = ?";
        String updateIdSql = "UPDATE " + safeTable + "Position SET ID = ? WHERE Position = ?";

        try (Connection con = setConnection()) {

            try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                ps.setInt(1, id);
                ps.setString(2, regNo);
                ps.setString(3, position);
                ps.setString(4, date);
                ps.setString(5, time);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(updateRegSql)) {
                ps.setString(1, regNo);
                ps.setString(2, position);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(updateIdSql)) {
                ps.setInt(1, id);
                ps.setString(2, position);
                ps.executeUpdate();
            }

            System.out.println("Data saved successfully");
        } catch (Exception e) {
            System.out.println("Error occurred while saving parking detail: " + e.getMessage());
        }
    }

    /**
     * Retrieves the ID and registration number stored at a given position.
     * Returns a 2-element array: [0] = id, [1] = registration number.
     */
    public String[] getData(String tableName, String position) {
        int id = 0;
        String regNo = "";
        String safeTable = sanitizeIdentifier(tableName);
        String sql = "SELECT * FROM " + safeTable + " WHERE Position = ?";

        try (Connection con = setConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, position);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    id = rs.getInt("ID");
                    regNo = rs.getString("RegNo");
                }
            }
        } catch (Exception e) {
            System.out.println("Error occurred while fetching data: " + e.getMessage());
        }

        return new String[]{String.valueOf(id), regNo};
    }

    /**
     * Records the checkout (TimeOut) for a given record ID.
     */
    public void updateTimeOut(String tableName, int id) {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss a");
        String time = dateFormat.format(new Date());
        String safeTable = sanitizeIdentifier(tableName);
        String sql = "UPDATE " + safeTable + " SET TimeOut = ? WHERE ID = ?";

        try (Connection con = setConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, time);
            ps.setInt(2, id);
            ps.executeUpdate();
            System.out.println("Time-out updated successfully");
        } catch (Exception e) {
            System.out.println("Error occurred while updating time-out: " + e.getMessage());
        }
    }

    /**
     * Frees up a parking position (marks it as empty / available again).
     */
    public void updateCarPosition(String tableName, String position) {
        String safeTable = sanitizeIdentifier(tableName);
        String updateRegSql = "UPDATE " + safeTable + " SET RegNo = '0' WHERE Position = ?";
        String updateIdSql = "UPDATE " + safeTable + " SET ID = 0 WHERE Position = ?";

        try (Connection con = setConnection()) {

            try (PreparedStatement ps = con.prepareStatement(updateRegSql)) {
                ps.setString(1, position);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(updateIdSql)) {
                ps.setString(1, position);
                ps.executeUpdate();
            }

            System.out.println("Position updated successfully");
        } catch (Exception e) {
            System.out.println("Error occurred while updating position: " + e.getMessage());
        }
    }

    /**
     * Finds the first available (empty) parking position for the table.
     */
    public String selectAvailablePosition(String tableName) {
        String position = "N/A";
        String safeTable = sanitizeIdentifier(tableName);
        String sql = "SELECT * FROM " + safeTable + " WHERE RegNo = '0'";

        try (Connection con = setConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                position = rs.getString("Position");
            }
        } catch (Exception e) {
            System.out.println("Error occurred while selecting position: " + e.getMessage());
        }
        return position;
    }

    /**
     * Restricts table-name input to a fixed allow-list. JDBC cannot
     * parameterize identifiers (table/column names), so the safest
     * defense against SQL injection through a table-name parameter is
     * to only ever allow known, expected values through.
     */
    private static String sanitizeIdentifier(String rawName) {
        if (rawName == null) {
            throw new IllegalArgumentException("Table name cannot be null");
        }
        switch (rawName) {
            case "Car":
            case "CarDetail":
            case "CarPosition":
            case "Bike":
            case "BikeDetail":
            case "BikePosition":
                return rawName;
            default:
                throw new IllegalArgumentException("Unrecognized table name: " + rawName);
        }
    }

    // ---- Backwards-compatible method names ----
    // These thin wrappers keep the original method names/signatures used
    // by the rest of the codebase working, while the real logic above
    // uses clearer names. This avoids having to rewrite every call site
    // in one big-bang change.
    public void InsertCarDetail(String tableName, int id, String reg, String pos) {
        insertParkingDetail(tableName, id, reg, pos);
    }

    public String[] GetData(String tableName, String pos) {
        return getData(tableName, pos);
    }

    public void UpdateTimeOut(String tableName, int id) {
        updateTimeOut(tableName, id);
    }

    public void UpdateCarPosition(String tableName, String pos) {
        updateCarPosition(tableName, pos);
    }

    public String SelectCarPosition(String tableName) {
        return selectAvailablePosition(tableName);
    }
}
