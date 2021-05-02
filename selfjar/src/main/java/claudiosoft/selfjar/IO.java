package claudiosoft.selfjar;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 *
 * @author claudio.tortorelli
 */
public class IO {

    private final File selfJarTmpFolder;
    private final File nextJar;
    private final Content contentEntries;

    private static IO io = null;

    public static IO get() throws SelfJarException {
        if (io != null) {
            return io;
        }
        io = new IO();
        return io;
    }

    private IO() throws SelfJarException {
        // use current date-time
        SimpleDateFormat sdf = new SimpleDateFormat(SelfConstants.DATE_FORMAT_SHORT);
        sdf.setTimeZone(SelfConstants.DEFAULT_TIMEZONE);
        String dateTime = sdf.format(new Date());

        // create temp out folder
        this.selfJarTmpFolder = new File(String.format("%s%s%s%s", System.getProperty("java.io.tmpdir"), File.separator, SelfConstants.TMP_SELFJAR_FOLDER, dateTime));
        this.nextJar = new File(String.format("%s%sselfJar%s.jar", System.getProperty("java.io.tmpdir"), File.separator, dateTime));
        this.contentEntries = new Content();
    }

    /**
     * Extract jar content to folder
     *
     * @throws IOException
     */
    public void out() throws IOException, SelfJarException {

        // build folder tree
        List<ContentEntry> content = this.contentEntries.getContentEntries();
        for (ContentEntry entry : content) {
            if (!entry.isDirectory()) {
                continue;
            }
            File folder = new File(String.format("%s%s%s", selfJarTmpFolder, File.separator, entry.getFullName()));
            folder.mkdirs();
        }

        // export content to folders
        InputStream is = null;
        FileOutputStream fos = null;
        JarFile jar = new JarFile(Identity.get().currentJar());
        for (ContentEntry entry : content) {
            if (entry.isDirectory()) {
                continue;
            }
            try {
                is = jar.getInputStream(entry);
                File outFile = new File(selfJarTmpFolder.getAbsoluteFile() + File.separator + entry.getName());
                fos = new FileOutputStream(outFile);
                SelfUtils.inputToOutput(is, fos);
                // keep open entries in workspace
                if (!entry.getFullName().startsWith("workspace")) {
                    entry.lockIn(outFile);
                }
            } finally {
                SelfUtils.closeQuietly(is);
                SelfUtils.closeQuietly(fos);
            }
        }
    }

    /**
     * Create a jar from folder
     *
     * @return
     * @throws SelfJarException
     * @throws IOException
     *
     * https://stackoverflow.com/questions/1281229/how-to-use-jaroutputstream-to-create-a-jar-file
     */
    public File in() throws SelfJarException, IOException {
        JarOutputStream target = null;
        try {
            target = new JarOutputStream(new FileOutputStream(nextJar));
            for (File nestedFile : selfJarTmpFolder.listFiles()) {
                addToNextJar(selfJarTmpFolder.getAbsolutePath(), nestedFile, target);
            }
        } finally {
            SelfUtils.closeQuietly(target);
        }
        return nextJar;
    }

    public void closeAll() throws IOException, SelfJarException {
        List<ContentEntry> content = this.contentEntries.getContentEntries();
        for (ContentEntry entry : content) {
            if (entry.isDirectory()) {
                continue;
            }
            entry.lockOut();
        }
        SelfUtils.deleteDirectory(selfJarTmpFolder);
    }

    public Context getContext() throws SelfJarException, IOException {
        return new Context(contentEntries.getContext());
    }

    @Override
    public String toString() {
        String ret = contentEntries.toString() + "\n";
        try {
            ret += getContext().toString() + "\n";
        } catch (Exception ex) {

        }
        return ret;
    }

    private void addToNextJar(String basePath, File source, JarOutputStream target) throws IOException, SelfJarException {
        BufferedInputStream in = null;
        try {
            if (source.isDirectory()) {
                String name = source.getPath().replace(basePath + File.separator, "");
                name = name.replace("\\", "/");
                if (!name.isEmpty()) {
                    if (!name.endsWith("/")) {
                        name += "/";
                    }
                    JarEntry entry = new JarEntry(name);
                    entry.setTime(source.lastModified());
                    target.putNextEntry(entry);
                    target.closeEntry();
                }
                for (File nestedFile : source.listFiles()) {
                    addToNextJar(basePath, nestedFile, target);
                }
                return;
            }

            String entryName = source.getPath().replace(basePath + File.separator, "");
            entryName = entryName.replace("\\", "/");

            // now unlock the entry...
            contentEntries.getContentEntry(entryName).lockOut();

            JarEntry jarEntry = new JarEntry(entryName);
            jarEntry.setTime(source.lastModified());
            target.putNextEntry(jarEntry);
            in = new BufferedInputStream(new FileInputStream(source));

            byte[] buffer = new byte[SelfConstants.BUFFER_SIZE];
            while (true) {
                int count = in.read(buffer);
                if (count == -1) {
                    break;
                }
                target.write(buffer, 0, count);
            }
            target.closeEntry();
        } finally {
            SelfUtils.closeQuietly(in);
        }
    }

}
