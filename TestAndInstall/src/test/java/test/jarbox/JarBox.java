/**
 * JarBoxProject - https://github.com/claudio-tortorelli/JarBox/
 *
 * MIT License - 2021
 */
package test.jarbox;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * Command line representation
 *
 * @author claudio.tortorelli
 */
public class JarBox {

    private static final String BASEPATH = "../jarbox/target";

    private static File sfInstance = null;

    private JarBox() throws IOException {
        File f = new File(BASEPATH);
        File[] matchingFiles = f.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith("JarBox") && name.endsWith(".jar");
            }
        });
        if (matchingFiles == null || matchingFiles.length != 1) {
            throw new IOException("JarBox not found");
        }
        sfInstance = matchingFiles[0];
    }

    public static File get() throws IOException {
        if (sfInstance == null) {
            new JarBox();
        }
        return sfInstance;
    }

    public static float size() {
        return sfInstance.length() / (float) (1024 * 1024);
    }

}
