package claudiosoft.selfgeneratingjar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.zip.ZipEntry;

/**
 *
 * @author claudio.tortorelli
 */
public class ContentEntry extends ZipEntry {

    private byte[] hash;
    private boolean core;

    private RandomAccessFile raf;
    private FileLock lock;

    public ContentEntry(ZipEntry entry, byte[] hash) {
        super(entry);
        this.core = CoreEntries.isCore(entry.getName());
        this.hash = hash;
        this.raf = null;
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

    public void lockOut() throws IOException {
        if (lock != null) {
            lock.release();
            raf.close();
        }
    }

    public void lockIn(File fileToLock) throws FileNotFoundException, SelfJarException {
        this.raf = new RandomAccessFile(fileToLock, "rw");
        try {
            this.lock = this.raf.getChannel().lock();
        } catch (IOException ex) {
            throw new SelfJarException(String.format("%s cannot be locked", fileToLock.getAbsolutePath()), ex);
        }
    }

    @Override
    public String toString() {
        return "ContentEntry{" + "hash=" + hash + ", core=" + core + super.toString() + '}';
    }

}
