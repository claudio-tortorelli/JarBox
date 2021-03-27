package claudiosoft.selfjar;

import claudiosoft.selfjar.commons.SelfConstants;
import claudiosoft.selfjar.commons.SelfJarException;
import claudiosoft.selfjar.commons.SelfUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

/**
 *
 * @author Claudio
 */
public class JarContext {

    private File contextFile;
    private long exeCount;

    public JarContext(File context) throws SelfJarException, IOException {
        this.contextFile = context;
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

    public void update() throws IOException {
        List<String> lines = Files.readAllLines(contextFile.toPath());
        FileOutputStream fos = new FileOutputStream(contextFile);
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
    }

    public File getContextFile() {
        return contextFile;
    }

    private void parse() throws IOException {
        List<String> lines = Files.readAllLines(contextFile.toPath());
        for (String line : lines) {
            if (line.startsWith(SelfConstants.CTX_COMMENT)) {
                continue;
            } else if (line.startsWith(SelfConstants.CTX_COUNT)) {
                exeCount = Integer.parseInt(line.split("=")[1]);
            }
        }
    }
}