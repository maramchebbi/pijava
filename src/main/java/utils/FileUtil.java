package utils;

import java.io.File;
import java.text.DecimalFormat;

public class FileUtil {

    /**
     * Convertit la taille d'un fichier en format lisible (KB, MB, etc.)
     * @param file Le fichier dont on veut connaître la taille
     * @return Une chaîne représentant la taille du fichier en format lisible
     */
    public static String getReadableFileSize(File file) {
        long size = file.length();
        if (size <= 0) return "0 B";

        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups))
                + " " + units[digitGroups];
    }

    /**
     * Vérifie si un fichier est une image
     * @param file Le fichier à vérifier
     * @return true si le fichier est une image, false sinon
     */
    public static boolean isImage(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                name.endsWith(".png") || name.endsWith(".gif") ||
                name.endsWith(".bmp");
    }

    /**
     * Vérifie si un fichier est un PDF
     * @param file Le fichier à vérifier
     * @return true si le fichier est un PDF, false sinon
     */
    public static boolean isPDF(File file) {
        return file.getName().toLowerCase().endsWith(".pdf");
    }

    /**
     * Obtient l'extension d'un fichier
     * @param file Le fichier dont on veut l'extension
     * @return L'extension du fichier
     */
    public static String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // pas d'extension
        }
        return name.substring(lastIndexOf + 1);
    }
}