package claudiosoft.selfgeneratingjar;

import java.io.File;

/**
 *
 * @author Claudio
 */
public class JarContext {

    private File parent;
    private int rebuildCount;
    private boolean verbose;

    public JarContext() {
        this.parent = null;
        this.rebuildCount = 0;
    }

    public File getParent() {
        return parent;
    }

    public void setParent(File file) {
        this.parent = file;
    }

    public int getRebuildCount() {
        return rebuildCount;
    }

    public void setRebuildCount(int rebuildCount) {
        this.rebuildCount = rebuildCount;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public String toString() {
        String ret = "--- Jar Context ---\n";
        ret += "I was rebuilt " + getRebuildCount() + " times\n";
        if (parent != null) {
            ret += "My parent is " + getParent().getAbsolutePath() + "\n";
        }
        return ret;
    }
}
