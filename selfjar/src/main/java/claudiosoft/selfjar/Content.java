package claudiosoft.selfjar;

import java.io.InputStream;
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

    @Override
    public String toString() {
        String ret = "=================\n"
                + "|   [CONTENT]   |\n"
                + "=================\n";
        ret += "I'm including following content" + "\n";
        for (ContentEntry entry : contentEntries) {
            String hash = "";
            if (entry.getHash() != null) {
                hash = SelfUtils.bytesToHex(entry.getHash());
            }
            ret += String.format("  %s %s\n", hash, entry.getFullName());
        }
        getContentEntries().toString();
        return ret + "\n";
    }

}
