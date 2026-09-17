package util;

import java.io.File;

/**
 * Resolves image file paths relative to the project folder, instead of
 * the original hardcoded absolute paths like:
 *   "D:\\hamza\\assignment\\NetBeansProjects\\Parking\\Pictures\\bike.jpg"
 *
 * Refactoring applied: Replace Hardcoded Path with Relative/Resolved Path.
 * Before: image icons only loaded on the original author's machine.
 * After: paths are resolved relative to the project's working directory,
 * so the same "Pictures/xxx.jpg" folder structure works on any machine.
 */
public final class ImagePaths {

    private ImagePaths() {
    }

    private static final String PICTURES_FOLDER = "Pictures";

    /**
     * @param fileName e.g. "bike.jpg", "car.jpg", "manager.jpg"
     * @return the resolved absolute path to that image in the
     *         project's Pictures folder.
     */
    public static String resolve(String fileName) {
        String projectDir = System.getProperty("user.dir");
        File imageFile = new File(projectDir, PICTURES_FOLDER + File.separator + fileName);

        if (!imageFile.exists()) {
            // fallback: try relative to current directory directly
            File alt = new File(PICTURES_FOLDER, fileName);
            if (alt.exists()) {
                imageFile = alt;
            }
        }
        return imageFile.getAbsolutePath();
    }
}
