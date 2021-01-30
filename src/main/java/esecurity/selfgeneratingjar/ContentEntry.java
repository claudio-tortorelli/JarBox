package esecurity.selfgeneratingjar;

import java.io.File;

/**
 *
 * @author claudio.tortorelli
 */
public class ContentEntry {

    private String name;
    private String path;
    private byte[] hash;
    private long size;
    private boolean isDirectory;

    public ContentEntry() {

    }

    public ContentEntry(String name, String path, long size, boolean isDir) {
        this.name = name;
        this.path = path;
        this.size = size;
        this.isDirectory = isDir;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isIsDirectory() {
        return isDirectory;
    }

    public void setIsDirectory(boolean isDirectory) {
        this.isDirectory = isDirectory;
    }

    public String getFullName() {
        return String.format("%s%s%s", path, File.separator, name);
    }

}
