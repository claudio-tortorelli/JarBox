package claudiosoft.selfgeneratingjar;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
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

    private static final List<ContentEntry> content = new LinkedList<>();

    public static enum CHECK_MODE {
        SIZE,
        ADD,
        SUB,
        ADD_SUB,
        ADD_SUB_COHERENCE,
        ALL
    }

    public static enum CHECK_TARGET {
        ALL,
        CORE_ONLY,
        EXTRA_ONLY
    }

    public Status() {
    }

    public static void init(Class mainClass) throws URISyntaxException, IOException, NoSuchAlgorithmException, SelfJarException {
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
        checkJarContent();
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

    public static final List<ContentEntry> getContent() {
        return content;
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

    public static void checkJarContent() throws IOException, NoSuchAlgorithmException, SelfJarException {
        checkJarContent(CHECK_MODE.ALL, CHECK_TARGET.ALL);
    }

    public static void checkJarContent(CHECK_MODE mode, CHECK_TARGET target) throws IOException, NoSuchAlgorithmException, SelfJarException {
        List<ContentEntry> curContent = null;
        if (!initialized) {
            curContent = content;
        } else {
            curContent = new LinkedList<>();
        }
        scanJarContent(curContent);
        if (!initialized) {
            return;
        }
        // compare previous content with current
        compareContents(curContent, mode, target);
    }

    public static void updateJarContent() throws IOException, NoSuchAlgorithmException {
        scanJarContent(content);
    }

    private static void scanJarContent(List<ContentEntry> curContent) throws IOException, NoSuchAlgorithmException {
        curContent.clear();
        JarFile jar = new JarFile(getCurrentJar());
        Enumeration<? extends JarEntry> enumeration = jar.entries();
        InputStream is = null;
        try {
            while (enumeration.hasMoreElements()) {
                ZipEntry zipEntry = enumeration.nextElement();
                byte[] hash = null;
                if (!zipEntry.isDirectory()) {
                    is = jar.getInputStream(zipEntry);
                    hash = Utils.getSHA256(is);
                }
                curContent.add(new ContentEntry(zipEntry.getName(), zipEntry.getSize(), zipEntry.isDirectory(), hash));
            }
        } finally {
            Utils.closeQuietly(is);
        }

        Collections.sort(curContent, new Comparator<ContentEntry>() {
            @Override
            public int compare(ContentEntry e1, ContentEntry e2) {
                return e1.getPath().compareTo(e2.getPath());
            }
        });
    }

    /**
     * suppose two list are sorted in same way
     */
    private static void compareContents(List<ContentEntry> actualContent, CHECK_MODE mode, CHECK_TARGET target) throws SelfJarException {
        if (mode.equals(CHECK_MODE.ALL) || mode.equals(CHECK_MODE.SIZE)) {
            if (actualContent.size() != content.size()) {
                throw new SelfJarException(String.format("actual content size is %d against %d expected", actualContent.size(), content.size()));
            }
        }

        if (mode.equals(CHECK_MODE.ALL) || mode.equals(CHECK_MODE.SUB) || mode.equals(CHECK_MODE.ADD_SUB)) {
            for (ContentEntry entry : content) {
                if (target.equals(CHECK_TARGET.CORE_ONLY) && !entry.isCore()) {
                    continue;
                } else if (target.equals(CHECK_TARGET.EXTRA_ONLY) && entry.isCore()) {
                    continue;
                }
                boolean found = false;
                for (ContentEntry entry2 : actualContent) {
                    if (entry.getPath().equals(entry2.getPath())) {
                        found = true;
                        if (mode.equals(CHECK_MODE.ADD_SUB_COHERENCE)) {
                            if (!Arrays.equals(entry.getHash(), entry2.getHash())) {
                                throw new SelfJarException(String.format("incoherent jar entry: %s", entry.getPath()));
                            }
                        }
                        break;
                    }
                }
                if (!found) {
                    throw new SelfJarException(String.format("missing entry %s", entry.getPath()));
                }
            }
        }
        if (mode.equals(CHECK_MODE.ALL) || mode.equals(CHECK_MODE.ADD) || mode.equals(CHECK_MODE.ADD_SUB)) {
            for (ContentEntry entry : actualContent) {
                boolean found = false;
                for (ContentEntry entry2 : content) {
                    if (entry.getPath().equals(entry2.getPath())) {
                        found = true;
                        if (mode.equals(CHECK_MODE.ADD_SUB_COHERENCE)) {
                            if (!Arrays.equals(entry.getHash(), entry2.getHash())) {
                                throw new SelfJarException(String.format("incoherent jar entry: %s", entry.getPath()));
                            }
                        }
                        break;
                    }
                }
                if (!found) {
                    throw new SelfJarException(String.format("entry %s was added", entry.getPath()));
                }
            }
        }
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

    // TODO, salvare lo status su file crittato da ripristinare all'avvio successivo
    // TODO, la chiave di cript/decript non può stare nel java stesso...
    // tool c++ che esegue la decifratura byte a byte
    private static void storeInternal() throws SelfJarException {
        throw new SelfJarException("not defined");
    }
}
