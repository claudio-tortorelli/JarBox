package claudiosoft.selfgeneratingjar;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author claudio.tortorelli
 */
public class JarIO {

    public JarIO() {

    }

    public void toFS(JarContent jarContent, File baseFolder) throws IOException {

        // build folder tree
        List<ContentEntry> content = jarContent.getContent();
        for (ContentEntry ent : content) {
            File folder = new File(String.format("%s%s%s", baseFolder, File.separator, ent.getFullName()));
            folder.mkdirs();
        }

        // export content to folders
//        JarFile jar = new JarFile(id.getCurrentJar());
//        Enumeration<? extends JarEntry> enumeration = jar.entries();
//        InputStream is = null;
//        FileOutputStream fos = null;
//        try {
//            while (enumeration.hasMoreElements()) {
//                ZipEntry zipEntry = enumeration.nextElement();
//                if (!zipEntry.isDirectory()) {
//                    is = jar.getInputStream(zipEntry);
//                    File outFile = new File(baseFolder.getAbsoluteFile() + File.separator + zipEntry.getName());
//                    fos = new FileOutputStream(outFile);
//                    Utils.inputToOutput(is, fos);
//                }
//            }
//        } finally {
//            Utils.closeQuietly(is);
//            Utils.closeQuietly(fos);
//        }
    }

    public void fromFS(List<ContentEntry> content, File baseFolder, File nextJar) throws SelfJarException {
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
