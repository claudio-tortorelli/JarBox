package claudiosoft.selfgeneratingjar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.zip.ZipEntry;

/**
 *
 * @author claudio.tortorelli
 */
public class ContentEntry extends ZipEntry {

    private byte[] hash;
    private boolean core;
    private FileInputStream fis;

    public ContentEntry(ZipEntry entry, byte[] hash) {
        super(entry);
        this.core = CoreEntries.isCore(entry.getName());
        this.hash = hash;
        this.fis = null;
    }

    public String getId() {
        return new File(getFullName()).getName();
    }

    public String getFullName() {
        return getName();
    }

    public byte[] getHash() {
        return hash;
    }

    public boolean isCore() {
        return core;
    }

    public FileInputStream getKeepOpen() {
        return fis;
    }

    public void keepOpen(File fileToLock) throws FileNotFoundException {
        this.fis = new FileInputStream(fileToLock);
    }

    @Override
    public String toString() {
        return "ContentEntry{" + "hash=" + hash + ", core=" + core + super.toString() + '}';
    }

}
