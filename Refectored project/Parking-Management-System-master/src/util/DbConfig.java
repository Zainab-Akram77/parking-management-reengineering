package util;

import java.io.File;
import java.net.URISyntaxException;

/**
 * Resolves the location of the Access database file relative to the
 * running application, instead of a hardcoded absolute path.
 *
 * Refactoring applied: Replace Hardcoded Path with Relative/Resolved Path.
 *
 * Before:
 *   String url = "jdbc:ucanaccess://D://6th Semester//SRE//Parking-Management-System-master//src//DataBase//parking.accdb";
 *   -> Only ever worked on the original author's "D:" drive.
 *
 * After:
 *   The path is resolved at runtime relative to where this class is
 *   actually running from (its code source location / classpath root),
 *   which is reliable regardless of how NetBeans or the packaged app
 *   sets the working directory. This fixes a real bug found during
 *   testing: System.getProperty("user.dir") is NOT guaranteed to be the
 *   project root - it depends on how the IDE/launcher starts the JVM,
 *   so the original relative-path fix could silently resolve to a
 *   non-existent file and fail with "Connection con is null".
 */
public final class DbConfig {

    private DbConfig() {
    }

    private static final String DB_RELATIVE_FROM_CLASSES =
            ".." + File.separator + "src" + File.separator + "DataBase" + File.separator + "parking.accdb";

    private static final String DB_RELATIVE_FROM_PROJECT_ROOT =
            "src" + File.separator + "DataBase" + File.separator + "parking.accdb";

    /**
     * Builds the JDBC URL for the Access database, resolving the .accdb
     * file using several fallback strategies so it works whether the
     * app is run from inside NetBeans, from the build/classes folder,
     * or from a packaged jar sitting next to the project folder.
     */
    public static String getDatabaseUrl() {
        File dbFile = locateDatabaseFile();
        String absolutePath = dbFile.getAbsolutePath();
        return "jdbc:ucanaccess://" + absolutePath;
    }

    private static File locateDatabaseFile() {
        // Strategy 1: relative to the folder this class is actually
        // running from (build/classes, or a packaged jar's folder).
        File fromCodeSource = resolveFromCodeSource();
        if (fromCodeSource != null && fromCodeSource.exists()) {
            return fromCodeSource;
        }

        // Strategy 2: relative to the current working directory,
        // assuming it IS the project root (works in many NetBeans
        // "Run" configurations).
        String projectDir = System.getProperty("user.dir");
        File fromUserDir = new File(projectDir, DB_RELATIVE_FROM_PROJECT_ROOT);
        if (fromUserDir.exists()) {
            return fromUserDir;
        }

        // Strategy 3: plain relative path from wherever the JVM
        // happens to be launched.
        File plainRelative = new File(DB_RELATIVE_FROM_PROJECT_ROOT);
        if (plainRelative.exists()) {
            return plainRelative;
        }

        // Nothing found - return the most likely candidate anyway so
        // the resulting error message at least shows a sensible path
        // instead of an empty one. The connection attempt will fail
        // clearly rather than silently pointing at a blank location.
        return fromUserDir;
    }

    /**
     * Finds the folder containing the compiled DbConfig.class file
     * (e.g. .../build/classes/) and walks up to the project root from
     * there, then down into src/DataBase/parking.accdb. This works
     * reliably regardless of what the JVM's working directory is set
     * to, because it is based on where the code itself physically is.
     */
    private static File resolveFromCodeSource() {
        try {
            File codeLocation = new File(
                    DbConfig.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI());

            // codeLocation is typically <project>/build/classes
            // (a directory) when run from NetBeans, so its parent's
            // parent is the project root.
            File classesDir = codeLocation.isDirectory() ? codeLocation : codeLocation.getParentFile();
            if (classesDir == null) {
                return null;
            }
            File buildDir = classesDir.getParentFile();
            File projectRoot = (buildDir != null) ? buildDir.getParentFile() : null;
            if (projectRoot == null) {
                return null;
            }
            return new File(projectRoot, DB_RELATIVE_FROM_PROJECT_ROOT);
        } catch (URISyntaxException | NullPointerException | SecurityException e) {
            return null;
        }
    }
}
