package test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * Command line representation
 *
 * @author claudio.tortorelli
 */
public class SelfJar {

    private static final String SJ_BASEPATH = "../selfjar/target";

    private static File sfInstance = null;

    private SelfJar() throws IOException {
        File f = new File(SJ_BASEPATH);
        File[] matchingFiles = f.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith("SelfJar") && name.endsWith(".jar");
            }
        });
        if (matchingFiles == null || matchingFiles.length != 1) {
            throw new IOException("SelfJar not found");
        }
        sfInstance = matchingFiles[0];
    }

    public static File get() throws IOException {
        if (sfInstance == null) {
            new SelfJar();
        }
        return sfInstance;
    }

}
