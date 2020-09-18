package utils;


public class PathUtils {
    public static String toAbsolute(String relativePath) {
        String absolutePath = relativePath;

        int offset = 0;

        if (relativePath.startsWith("..")) {
            offset = 2;
        } else if (relativePath.startsWith(".")) {
            offset = 1;
        }

        if (offset > 0) {
                String workspace = "";
                int index = -1;
                if (Utils.isWindows()) {
                    index = workspace.indexOf("\\aka-working-space");
                } else if (Utils.isUnix() || Utils.isMac()) {
                    index = workspace.indexOf("/aka-working-space");
                }
                if (index < 0) {
                    if (Utils.isWindows()) {
                    } else if (Utils.isUnix() || Utils.isMac()) {
                    }
                    absolutePath = relativePath;
                }
                else {
                    String prefix = workspace.substring(0, index);
                    absolutePath = prefix + relativePath.substring(offset);
            }
        }


        return absolutePath;
    }

    public static String toRelative(String absolutePath) {
        if (absolutePath == null)
            return "";

        String workspace = "";//

        int index = -1;
        if (Utils.isWindows()) {
            index = workspace.indexOf("\\aka-working-space");
        } else if (Utils.isUnix() || Utils.isMac()) {
            index = workspace.indexOf("/aka-working-space");
        }

        if (index < 0) {
            if (Utils.isWindows()) {
            } else if (Utils.isUnix() || Utils.isMac()) {
            }
            return absolutePath;
        }

        String prefix = workspace.substring(0, index);
        return absolutePath.replaceFirst("\\Q" + prefix + "\\E", ".");
    }
}
