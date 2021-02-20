package claudiosoft.selfgeneratingjar;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 *
 * @author claudio.tortorelli
 */
public class JarIO {

    public JarIO() {

    }

    public void out(JarContent jarContent, File selfJarFolder) throws IOException {

        // build folder tree
        List<ContentEntry> content = jarContent.getContent();
        for (ContentEntry entry : content) {
            if (!entry.isDirectory()) {
                continue;
            }
            File folder = new File(String.format("%s%s%s", selfJarFolder, File.separator, entry.getFullName()));
            folder.mkdirs();
        }

        // export content to folders
        InputStream is = null;
        FileOutputStream fos = null;
        JarFile jar = new JarFile(jarContent.getJarFile());
        for (ContentEntry entry : content) {
            if (entry.isDirectory()) {
                continue;
            }
            try {
                is = jar.getInputStream(entry);
                File outFile = new File(selfJarFolder.getAbsoluteFile() + File.separator + entry.getName());
                fos = new FileOutputStream(outFile);
                Utils.inputToOutput(is, fos);
            } finally {
                Utils.closeQuietly(is);
                Utils.closeQuietly(fos);
            }
        }
    }

    // https://stackoverflow.com/questions/1281229/how-to-use-jaroutputstream-to-create-a-jar-file
    public File in(File selfJarFolder) throws SelfJarException, IOException {

        File nextJar = File.createTempFile("selfJar", ".jar");

//        Manifest manifest = new Manifest();
//        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        JarOutputStream target = null;
        try {
            //target = new JarOutputStream(new FileOutputStream(nextJar), manifest);
            target = new JarOutputStream(new FileOutputStream(nextJar));
            for (File nestedFile : selfJarFolder.listFiles()) {
                add(selfJarFolder.getAbsolutePath(), nestedFile, target);
            }
        } finally {
            Utils.closeQuietly(target);
        }
        return nextJar;
    }

    private void add(String basePath, File source, JarOutputStream target) throws IOException {
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
                    add(basePath, nestedFile, target);
                }
                return;
            }

            String entryName = source.getPath().replace(basePath + File.separator, "");
            entryName = entryName.replace("\\", "/");
            JarEntry entry = new JarEntry(entryName);
            entry.setTime(source.lastModified());
            target.putNextEntry(entry);
            in = new BufferedInputStream(new FileInputStream(source));

            byte[] buffer = new byte[Constants.BUFFER_SIZE];
            while (true) {
                int count = in.read(buffer);
                if (count == -1) {
                    break;
                }
                target.write(buffer, 0, count);
            }
            target.closeEntry();
        } finally {
            Utils.closeQuietly(in);
        }
    }
}
