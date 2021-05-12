package claudiosoft.selfjar;

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
 * @author claudio.tortorelli
 */
public class Content {

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

    private final List<ContentEntry> contentEntries;

    public Content() throws SelfJarException {
        this(true);
    }

    public Content(boolean sort) throws SelfJarException {
        try {
            this.contentEntries = new LinkedList<>();
            JarFile jarFile = new JarFile(Identity.get().currentJar());
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
                    this.contentEntries.add(new ContentEntry(zipEntry, hash));
                }
            } finally {
                SelfUtils.closeQuietly(is);
            }

            Collections.sort(this.contentEntries, new Comparator<ContentEntry>() {
                @Override
                public int compare(ContentEntry e1, ContentEntry e2) {
                    return e1.getFullName().compareTo(e2.getFullName());
                }
            });
        } catch (Exception ex) {
            throw new SelfJarException(ex.getMessage(), ex);
        }
    }

    public final List<ContentEntry> getContentEntries() {
        return contentEntries;
    }

    public final ContentEntry getContentEntry(String entryFullName) throws SelfJarException {
        for (ContentEntry entry : contentEntries) {
            if (entry.getFullName().equals(entryFullName)) {
                return entry;
            }
        }
        throw new SelfJarException("no entry found with full name " + entryFullName);
    }

    public final ContentEntry getContext() throws SelfJarException {
        return getContentEntry(Context.CONTEXT_FULLNAME);
    }

    /**
     * suppose two list are sorted in same way
     *
     * @param other
     * @param mode
     * @param target
     */
    public void compareContents(Content other, CHECK_MODE mode, CHECK_TARGET target) throws SelfJarException {
        List<ContentEntry> otherContent = other.getContentEntries();
        if (mode.equals(CHECK_MODE.ALL) || mode.equals(CHECK_MODE.SIZE)) {
            if (otherContent.size() != contentEntries.size()) {
                throw new SelfJarException(String.format("actual content size is %d against %d expected", otherContent.size(), contentEntries.size()));
            }
        }

        if (mode.equals(CHECK_MODE.ALL) || mode.equals(CHECK_MODE.SUB) || mode.equals(CHECK_MODE.ADD_SUB)) {
            for (ContentEntry entry : contentEntries) {
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
                for (ContentEntry entry2 : contentEntries) {
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
        for (ContentEntry entry : contentEntries) {
            String hash = "";
            if (entry.getHash() != null) {
                hash = SelfUtils.bytesToHex(entry.getHash());
            }
            ret += String.format("  %s %s\n", hash, entry.getFullName());
        }
        getContentEntries().toString();
        return ret;
    }

}
