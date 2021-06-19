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

    private final List<ContentEntry> contentEntries;

    public Content() throws SelfJarException {
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
                        hash = Utils.getSHA256(is);
                        Utils.closeQuietly(is);
                    }
                    this.contentEntries.add(new ContentEntry(zipEntry, hash));
                }
            } finally {
                Utils.closeQuietly(is);
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

    public final List<ContentEntry> getEntries() {
        return contentEntries;
    }

    public final ContentEntry getEntry(String entryFullName) throws SelfJarException {
        for (ContentEntry entry : contentEntries) {
            if (entry.getFullName().equals(entryFullName)) {
                return entry;
            }
        }
        throw new SelfJarException("no entry found with full name " + entryFullName);
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
                hash = Utils.bytesToHex(entry.getHash());
            }
            ret += String.format("  %s %s\n", hash, entry.getFullName());
        }
        getEntries().toString();
        return ret + "\n";
    }

}
