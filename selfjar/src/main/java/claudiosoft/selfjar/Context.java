package claudiosoft.selfjar;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author claudio.tortorelli
 */
public class Context {

    private ContentEntry contextEntry;
    private HashMap<String, String> envEntries;
    private HashMap<String, String> jobParamsEntries;

    private long exeCount;

    public Context(ContentEntry entry) throws SelfJarException, IOException {
        if (!entry.getFullName().equals(SelfConstants.CONTEXT_FULLNAME)) {
            throw new SelfJarException("invalid context entry");
        }
        contextEntry = entry;
        envEntries = new HashMap<>();
        jobParamsEntries = new HashMap<>();
        parse();
    }

    @Override
    public String toString() {
        String ret = "--- Jar Context ---\n";
        ret += "I was executed " + exeCount + " times\n";

        if (!envEntries.isEmpty()) {
            ret += "There are environment variables:\n";
            for (Map.Entry<String, String> set : envEntries.entrySet()) {
                if (set.getValue().isEmpty()) {
                    ret += String.format("%s\n", set.getKey());
                } else {
                    ret += String.format("%s=%s\n", set.getKey(), set.getValue());
                }

            }
        }
        if (!jobParamsEntries.isEmpty()) {
            ret += "There are JVM variables:\n";
            for (Map.Entry<String, String> set : jobParamsEntries.entrySet()) {
                if (set.getValue().isEmpty()) {
                    ret += String.format("%s\n", set.getKey());
                } else {
                    ret += String.format("%s=%s\n", set.getKey(), set.getValue());
                }
            }
        }
        return ret;
    }

    public void update() throws IOException, SelfJarException {
        FileOutputStream fos = null;
        try {
            exeCount++;

            contextEntry.lockOut();
            fos = new FileOutputStream(contextEntry.getFile().getAbsolutePath());
            fos.write(String.format("%s=%d", SelfConstants.CTX_COUNT, exeCount).getBytes(Charset.forName("UTF-8")));
            fos.write("\n".getBytes(Charset.forName("UTF-8")));
            for (Map.Entry<String, String> set : envEntries.entrySet()) {
                if (set.getValue().isEmpty()) {
                    fos.write(String.format("%s%s\n", SelfConstants.CTX_ENVPARAM, set.getKey()).getBytes(Charset.forName("UTF-8")));
                } else {
                    fos.write(String.format("%s%s=%s\n", SelfConstants.CTX_ENVPARAM, set.getKey(), set.getValue()).getBytes(Charset.forName("UTF-8")));
                }
                fos.write("\n".getBytes(Charset.forName("UTF-8")));
            }
            for (Map.Entry<String, String> set : jobParamsEntries.entrySet()) {
                if (set.getValue().isEmpty()) {
                    fos.write(String.format("%s%s\n", SelfConstants.CTX_JOBPARAM, set.getKey()).getBytes(Charset.forName("UTF-8")));
                } else {
                    fos.write(String.format("%s%s=%s\n", SelfConstants.CTX_JOBPARAM, set.getKey(), set.getValue()).getBytes(Charset.forName("UTF-8")));
                }

                fos.write("\n".getBytes(Charset.forName("UTF-8")));
            }
        } finally {
            SelfUtils.closeQuietly(fos);
            contextEntry.lockIn(contextEntry.getFile());
        }
    }

    public void applyParams(SelfParams params) {

        for (String var : params.addEnv()) {
            String[] splitted = var.split("=");
            String key = splitted[0];
            String value = "";
            if (splitted.length > 1) {
                value = splitted[1];
            }
            envEntries.put(key, value);
        }
        for (String var : params.addPar()) {
            String[] splitted = var.split("=");
            String key = splitted[0];
            String value = "";
            if (splitted.length > 1) {
                value = splitted[1];
            }
            jobParamsEntries.put(key, value);
        }
        for (String var : params.delEnv()) {
            if (envEntries.containsKey(var)) {
                envEntries.remove(var);
            }
        }
        for (String var : params.delPar()) {
            if (jobParamsEntries.containsKey(var)) {
                jobParamsEntries.remove(var);
            }
        }
    }

    public final HashMap<String, String> getEnvEntries() {
        return envEntries;
    }

    public final HashMap<String, String> getJobParamsEntries() {
        return jobParamsEntries;
    }

    public long getExeCount() {
        return exeCount;
    }

    private void parse() throws IOException, SelfJarException {
        try {
            contextEntry.lockOut();
            List<String> lines = Files.readAllLines(contextEntry.getFile().toPath());
            for (String line : lines) {
                if (line.startsWith(SelfConstants.CTX_COMMENT) || line.isEmpty()) {
                    continue;
                } else if (line.startsWith(SelfConstants.CTX_COUNT)) {
                    exeCount = Integer.parseInt(line.split("=")[1]);
                } else if (line.startsWith(SelfConstants.CTX_ENVPARAM)) {
                    String[] splitted = line.substring(SelfConstants.CTX_ENVPARAM.length()).split("=");
                    String key = splitted[0];
                    String value = "";
                    if (splitted.length > 1) {
                        value = splitted[1];
                    }
                    envEntries.put(key, value);
                } else if (line.startsWith(SelfConstants.CTX_JOBPARAM)) {
                    String[] splitted = line.substring(SelfConstants.CTX_JOBPARAM.length()).split("=");
                    String key = splitted[0];
                    String value = "";
                    if (splitted.length > 1) {
                        value = splitted[1];
                    }
                    jobParamsEntries.put(key, value);
                } else {
                    throw new SelfJarException("Invalid context entry");
                }
            }
        } finally {
            contextEntry.lockIn(contextEntry.getFile());
        }
    }
}
