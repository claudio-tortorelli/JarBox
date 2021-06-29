/**
 * JarBoxProject - https://github.com/claudio-tortorelli/JarBox/
 *
 * MIT License - 2021
 */
package claudiosoft.jarbox;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.zip.ZipEntry;

/**
 * A single JarBox's entry, deployed inside user temporary folder. A "core"
 * entry isn't included under job or context folder and it is locked by file
 * system. Every entry has a status SHA256 hash
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

    public final File getFile() throws JarBoxException {
        if (tmpFile == null) {
            throw new JarBoxException("entry file undefined");
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

    public void lockIn() throws FileNotFoundException, JarBoxException {
        this.lockIn(null);
    }

    public void lockIn(File fileToLock) throws FileNotFoundException, JarBoxException {
        if (fileToLock != null) {
            tmpFile = fileToLock;
        }
        if (tmpFile == null) {
            throw new JarBoxException("no file to lock is specified for content entry " + getName());
        }

        raf = new RandomAccessFile(tmpFile, "rw");
        try {
            lock = raf.getChannel().lock();
        } catch (IOException ex) {
            throw new JarBoxException(String.format("%s cannot be locked", tmpFile.getAbsolutePath()), ex);
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
        if (entryPath.startsWith(Constants.JOB_FOLDER)) {
            return false;
        }
        if (entryPath.startsWith(Constants.CONTEXT_FOLDER)) {
            return false;
        }
        return true;
    }
}
