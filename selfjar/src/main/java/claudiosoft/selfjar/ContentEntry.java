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

    private final byte[] hash;
    private final boolean core;

    private RandomAccessFile raf;
    private FileLock lock;
    private File tmpFile;

    public ContentEntry(ZipEntry entry, byte[] hash) {
        super(entry);
        this.core = isCore(entry.getName());
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

    public void lockIn() throws FileNotFoundException, SelfJarException {
        this.lockIn(null);
    }

    public void lockIn(File fileToLock) throws FileNotFoundException, SelfJarException {
        if (fileToLock != null) {
            tmpFile = fileToLock;
        }
        if (tmpFile == null) {
            throw new SelfJarException("no file to lock is specified for content entry " + getName());
        }

        raf = new RandomAccessFile(tmpFile, "rw");
        try {
            lock = raf.getChannel().lock();
        } catch (IOException ex) {
            throw new SelfJarException(String.format("%s cannot be locked", tmpFile.getAbsolutePath()), ex);
        }
    }

    public boolean isLocked() {
        return lock != null;
    }

    @Override
    public String toString() {
        return "ContentEntry{" + "hash=" + hash + ", core=" + core + super.toString() + '}';
    }

    private boolean isCore(String entryPath) {
        if (entryPath.startsWith(SelfConstants.JOB_FOLDER)) {
            return false;
        }
        if (entryPath.startsWith(SelfConstants.CONTEXT_FOLDER)) {
            return false;
        }
        return true;
    }
}
