package claudiosoft.selfgeneratingjar;

/**
 *
 * @author claudio.tortorelli
 */
public class ContentEntry {

    private String name;
    private String path;
    private byte[] hash;
    private long size;
    private boolean directory;
    private boolean core;

    public ContentEntry() {

    }

    public ContentEntry(String name, String path, long size, boolean isDir, byte[] hash) {
        this.name = name;
        this.path = path;
        this.size = size;
        this.directory = isDir;
        this.core = CoreEntries.isCore(path);
        this.hash = hash;
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

    public boolean isDirectory() {
        return directory;
    }

    public void setDirectory(boolean isDirectory) {
        this.directory = isDirectory;
    }

    public boolean isCore() {
        return core;
    }

    @Override
    public String toString() {
        return "ContentEntry{" + "name=" + name + ", path=" + path + ", hash=" + hash + ", size=" + size + ", directory=" + directory + ", core=" + core + '}';
    }

}
