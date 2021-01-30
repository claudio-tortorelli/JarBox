package claudiosoft.selfgeneratingjar;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 *
 * @author Claudio
 */
public class Status {

    private static File parent = null;
    private static String fullName = "";
    private static String className = "";
    private static String parentFolderJar = "";
    private static String jarName = "";
    private static File currentJar = null;
    private static String javaRuntime = "";
    private static int rebuildCount = 0;

    private static boolean initialized = false;

    private static List<ContentEntry> content = new LinkedList<>();

    public Status() {
    }

    public static void init(Class mainClass) throws URISyntaxException, IOException, NoSuchAlgorithmException {
        if (initialized) {
            return;
        }
        setFullName(mainClass.getName());
        setClassName(mainClass.getSimpleName());
        setParentFolderJar(new File(mainClass.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getAbsolutePath());
        String curName = new File(mainClass.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getName();
        if (curName.equals("classes")) {
            String[] list = new File(getParentFolderJar()).list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".jar");
                }
            });
            if (list.length > 0) {
                curName = list[0];
            }
        }
        setJarName(curName);
        setCurrentJar(new File(getParentFolderJar() + File.separator + getJarName()));
        setJavaRuntime(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
        scanJarContent();
        initialized = true;
    }

    public static File getParent() {
        return parent;
    }

    public static void setParent(File file) {
        Status.parent = file;
    }

    public static String getFullName() {
        return fullName;
    }

    public static void setFullName(String fullName) {
        Status.fullName = fullName;
    }

    public static String getClassName() {
        return className;
    }

    public static void setClassName(String className) {
        Status.className = className;
    }

    public static String getParentFolderJar() {
        return parentFolderJar;
    }

    public static void setParentFolderJar(String parentFolderJar) {
        Status.parentFolderJar = parentFolderJar;
    }

    public static String getJarName() {
        return jarName;
    }

    public static void setJarName(String jarName) {
        Status.jarName = jarName;
    }

    public static File getCurrentJar() {
        return currentJar;
    }

    public static void setCurrentJar(File currentJar) {
        Status.currentJar = currentJar;
    }

    public static String getJavaRuntime() {
        return javaRuntime;
    }

    public static void setJavaRuntime(String javaRuntime) {
        Status.javaRuntime = javaRuntime;
    }

    public static int getRebuildCount() {
        return rebuildCount;
    }

    public static void setRebuildCount(int rebuildCount) {
        Status.rebuildCount = rebuildCount;
    }

    /**
     *
     * @return
     */
    public static String print() throws IOException {
        if (!initialized) {
            return "not_initialized";
        }
        String ret = "--- Jar Info ---\n";
        ret += "My full name is " + getFullName() + "\n";
        ret += "My simple name is " + getClassName() + "\n";
        ret += "I'm contained into " + getCurrentJar().getAbsolutePath() + "\n";
        ret += "I'm executing by JVM " + getJavaRuntime() + "\n";
        ret += "I was rebuilt " + getRebuildCount() + " times\n";
        ret += "I'm including following content" + "\n";
        ret += printContentList();
        return ret;
    }

    public static void print(BasicConsoleLogger logger) throws IOException {
        logger.info(print());
    }

    private static void scanJarContent() throws IOException, NoSuchAlgorithmException {
        content.clear();
        JarFile jar = new JarFile(getCurrentJar());
        Enumeration<? extends JarEntry> enumeration = jar.entries();
        while (enumeration.hasMoreElements()) {
            ZipEntry zipEntry = enumeration.nextElement();
            InputStream is = null;
            byte[] hash = null;
            try {
                if (!zipEntry.isDirectory()) {
                    is = jar.getInputStream(zipEntry);
                    hash = Utils.getSHA256(is);
                }
                content.add(new ContentEntry(zipEntry.getName(), zipEntry.getName(), zipEntry.getSize(), zipEntry.isDirectory(), hash));
            } finally {
                Utils.closeQuietly(is);
            }
        }

        Collections.sort(content, new Comparator<ContentEntry>() {
            @Override
            public int compare(ContentEntry e1, ContentEntry e2) {
                return e1.getPath().compareTo(e2.getPath());
            }
        });
    }

    private static String printContentList() {
        String ret = "";
        for (ContentEntry entry : content) {
            String hash = "";
            if (entry.getHash() != null) {
                hash = Utils.bytesToHex(entry.getHash());
            }
            ret += String.format("  %s %s\n", hash, entry.getPath());
        }
        return ret;
    }
}
