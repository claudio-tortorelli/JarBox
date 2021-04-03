package claudiosoft.selfjar;

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
    private File tmpFile;

    public ContentEntry(ZipEntry entry, byte[] hash) {
        super(entry);
        this.core = CoreEntries.isCore(entry.getName());
        this.hash = hash;
        this.raf = null;
        this.tmpFile = null;
    }

    public String getShortName() {
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

    public final File getFile() throws SelfJarException {
        if (tmpFile == null) {
            throw new SelfJarException("entry file undefined");
        }
        return tmpFile;
    }

    public void lockOut() throws IOException {
        if (lock == null) {
            return;
        }
        lock.release();
        raf.close();
        lock = null;
    }

    public void lockIn(File fileToLock) throws FileNotFoundException, SelfJarException {
        this.tmpFile = fileToLock;
        this.raf = new RandomAccessFile(fileToLock, "rw");
        try {
            this.lock = this.raf.getChannel().lock();
        } catch (IOException ex) {
            throw new SelfJarException(String.format("%s cannot be locked", fileToLock.getAbsolutePath()), ex);
        }
    }

    public boolean isLocked() {
        if (lock == null) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ContentEntry{" + "hash=" + hash + ", core=" + core + super.toString() + '}';
    }

}
