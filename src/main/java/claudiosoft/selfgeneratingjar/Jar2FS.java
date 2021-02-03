package claudiosoft.selfgeneratingjar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 *
 * @author claudio.tortorelli
 */
public class Jar2FS {

    public Jar2FS() {

    }

    public static void jar2Fs(File currentJar, List<ContentEntry> content, File baseFolder) throws IOException {
        JarFile jar = new JarFile(currentJar);
        Enumeration<? extends JarEntry> enumeration = jar.entries();
        InputStream is = null;
        try {
            while (enumeration.hasMoreElements()) {
                ZipEntry zipEntry = enumeration.nextElement();
                if (!zipEntry.isDirectory()) {
                    is = jar.getInputStream(zipEntry);
                } else {
                    //Files.createDirectory(zipEntry.);
                }
            }
        } finally {
            Utils.closeQuietly(is);
        }
    }

    public static void fs2Jar(List<ContentEntry> content, File baseFolder, File nextJar) throws SelfJarException {
        if (nextJar.exists()) {
            throw new SelfJarException("target jar already exists");
        }
        /**
         * sample
         *
         * FileOutputStream fout = new FileOutputStream("c:/tmp/foo.jar");
         * JarOutputStream jarOut = new JarOutputStream(fout);
         * jarOut.putNextEntry(new ZipEntry("com/foo/")); // Folders must end
         * with "/". jarOut.putNextEntry(new ZipEntry("com/foo/Foo.class"));
         * jarOut.write(getBytes("com/foo/Foo.class")); jarOut.closeEntry();
         * jarOut.putNextEntry(new ZipEntry("com/foo/Bar.class"));
         * jarOut.write(getBytes("com/foo/Bar.class")); jarOut.closeEntry();
         * jarOut.close(); fout.close();
         */
    }
}
