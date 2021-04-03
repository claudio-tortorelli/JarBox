package claudiosoft.selfjar;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

/**
 *
 * @author claudio.tortorelli
 */
public class Context {

    private ContentEntry contextEntry;

    private long exeCount;

    public Context(ContentEntry entry) throws SelfJarException, IOException {
        if (!entry.getFullName().equals(SelfConstants.CONTEXT_FULLNAME)) {
            throw new SelfJarException("invalid context entry");
        }
        contextEntry = entry;
        parse();
    }

    public long getExeCount() {
        return exeCount;
    }

    public void setExeCount(long exeCount) {
        this.exeCount = exeCount;
    }

    @Override
    public String toString() {
        String ret = "--- Jar Context ---\n";
        ret += "I was executed " + getExeCount() + " times\n";
        return ret;
    }

    public void update() throws IOException, SelfJarException {
        try {
            contextEntry.lockOut();
            List<String> lines = Files.readAllLines(contextEntry.getFile().toPath());
            FileOutputStream fos = new FileOutputStream(contextEntry.getFile().getAbsolutePath());
            try {
                for (String line : lines) {
                    if (line.startsWith(SelfConstants.CTX_COMMENT)) {
                        fos.write(line.getBytes(Charset.forName("UTF-8")));
                    } else if (line.startsWith(SelfConstants.CTX_COUNT)) {
                        fos.write(String.format("%s=%d", SelfConstants.CTX_COUNT, exeCount).getBytes(Charset.forName("UTF-8")));
                    }
                    fos.write("\n".getBytes(Charset.forName("UTF-8")));
                }

            } finally {
                SelfUtils.closeQuietly(fos);
            }
        } finally {
            contextEntry.lockIn(contextEntry.getFile());
        }
    }

    private void parse() throws IOException, SelfJarException {
        try {
            contextEntry.lockOut();
            List<String> lines = Files.readAllLines(contextEntry.getFile().toPath());
            for (String line : lines) {
                if (line.startsWith(SelfConstants.CTX_COMMENT)) {
                    continue;
                } else if (line.startsWith(SelfConstants.CTX_COUNT)) {
                    exeCount = Integer.parseInt(line.split("=")[1]);
                }
            }
        } finally {
            contextEntry.lockIn(contextEntry.getFile());
        }
    }
}
