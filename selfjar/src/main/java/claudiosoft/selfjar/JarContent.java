package claudiosoft.selfjar;

import claudiosoft.selfjar.commons.SelfJarException;
import claudiosoft.selfjar.commons.SelfUtils;
import java.io.File;
import java.io.InputStream;
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
public class JarContent {

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

    private final List<ContentEntry> content;
    private final File jarFile;

    public JarContent(File jar) throws SelfJarException {
        this(jar, true);
    }

    public JarContent(File jar, boolean sort) throws SelfJarException {
        try {
            this.jarFile = jar;
            this.content = new LinkedList<>();
            JarFile jarFile = new JarFile(jar);
            Enumeration<? extends JarEntry> enumeration = jarFile.entries();
            InputStream is = null;
            try {
                while (enumeration.hasMoreElements()) {
                    ZipEntry zipEntry = enumeration.nextElement();
                    byte[] hash = null;
                    if (!zipEntry.isDirectory()) {
                        is = jarFile.getInputStream(zipEntry);
                        hash = SelfUtils.getSHA256(is);
                        SelfUtils.closeQuietly(is);
                    }
                    this.content.add(new ContentEntry(zipEntry, hash));
                }
            } finally {
                SelfUtils.closeQuietly(is);
            }

            Collections.sort(this.content, new Comparator<ContentEntry>() {
                @Override
                public int compare(ContentEntry e1, ContentEntry e2) {
                    return e1.getFullName().compareTo(e2.getFullName());
                }
            });
        } catch (Exception ex) {
            throw new SelfJarException(ex.getMessage(), ex);
        }
    }

    public final List<ContentEntry> getContent() {
        return content;
    }

    public File getJarFile() {
        return jarFile;
    }

    /**
     * suppose two list are sorted in same way
     *
     * @param other
     * @param mode
     * @param target
     */
    public void compareContents(JarContent other, CHECK_MODE mode, CHECK_TARGET target) throws SelfJarException {
        List<ContentEntry> otherContent = other.getContent();
        if (mode.equals(CHECK_MODE.ALL) || mode.equals(CHECK_MODE.SIZE)) {
            if (otherContent.size() != content.size()) {
                throw new SelfJarException(String.format("actual content size is %d against %d expected", otherContent.size(), content.size()));
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
                for (ContentEntry entry2 : otherContent) {
                    if (entry.getFullName().equals(entry2.getFullName())) {
                        found = true;
                        if (mode.equals(CHECK_MODE.ADD_SUB_COHERENCE)) {
                            if (!Arrays.equals(entry.getHash(), entry2.getHash())) {
                                throw new SelfJarException(String.format("incoherent jar entry: %s", entry.getFullName()));
                            }
                        }
                        break;
                    }
                }
                if (!found) {
                    throw new SelfJarException(String.format("missing entry %s", entry.getFullName()));
                }
            }
        }
        if (mode.equals(CHECK_MODE.ALL) || mode.equals(CHECK_MODE.ADD) || mode.equals(CHECK_MODE.ADD_SUB)) {
            for (ContentEntry entry : otherContent) {
                boolean found = false;
                for (ContentEntry entry2 : content) {
                    if (entry.getFullName().equals(entry2.getFullName())) {
                        found = true;
                        if (mode.equals(CHECK_MODE.ADD_SUB_COHERENCE)) {
                            if (!Arrays.equals(entry.getHash(), entry2.getHash())) {
                                throw new SelfJarException(String.format("incoherent jar entry: %s", entry.getFullName()));
                            }
                        }
                        break;
                    }
                }
                if (!found) {
                    throw new SelfJarException(String.format("entry %s was added", entry.getFullName()));
                }
            }
        }
    }

    @Override
    public String toString() {
        String ret = "--- Jar Content ---\n";
        ret += "I'm including following content" + "\n";
        for (ContentEntry entry : content) {
            String hash = "";
            if (entry.getHash() != null) {
                hash = SelfUtils.bytesToHex(entry.getHash());
            }
            ret += String.format("  %s %s\n", hash, entry.getFullName());
        }
        getContent().toString();
        return ret;
    }

}
