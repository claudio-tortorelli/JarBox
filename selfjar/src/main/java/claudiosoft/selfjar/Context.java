package claudiosoft.selfjar;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
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
    private boolean jobInstalled;
    private String main;

    private long exeCount = -1;

    public static final String CONTEXT_FULLNAME = "context/context.txt";
    public static final String CTX_COUNT = "EXECOUNT:";
    public static final String CTX_INSTALLJOB = "JOB:";
    public static final String CTX_JOBPARAM = "JOBPAR:";
    public static final String CTX_ENVPARAM = "ENVPAR:";
    public static final String CTX_MAIN = "MAIN:";

    public Context(ContentEntry entry) throws SelfJarException, IOException {
        if (!entry.getFullName().equals(CONTEXT_FULLNAME)) {
            throw new SelfJarException("invalid context entry");
        }
        jobInstalled = false;
        main = "";
        contextEntry = entry;
        envEntries = new HashMap<>();
        jobParamsEntries = new HashMap<>();
        parse();
    }

    public void update() throws IOException, SelfJarException {
        FileOutputStream fos = null;
        try {
            exeCount++;

            contextEntry.lockOut();
            fos = new FileOutputStream(contextEntry.getFile().getAbsolutePath());
            fos.write(String.format("%s%d\n", CTX_COUNT, exeCount).getBytes(Charset.forName("UTF-8")));
            for (Map.Entry<String, String> set : envEntries.entrySet()) {
                if (set.getValue().isEmpty()) {
                    fos.write(String.format("%s%s\n", CTX_ENVPARAM, set.getKey()).getBytes(Charset.forName("UTF-8")));
                } else {
                    fos.write(String.format("%s%s=%s\n", CTX_ENVPARAM, set.getKey(), set.getValue()).getBytes(Charset.forName("UTF-8")));
                }
            }
            for (Map.Entry<String, String> set : jobParamsEntries.entrySet()) {
                if (set.getValue().isEmpty()) {
                    fos.write(String.format("%s%s\n", CTX_JOBPARAM, set.getKey()).getBytes(Charset.forName("UTF-8")));
                } else {
                    fos.write(String.format("%s%s=%s\n", CTX_JOBPARAM, set.getKey(), set.getValue()).getBytes(Charset.forName("UTF-8")));
                }
            }
            if (jobInstalled) {
                fos.write(String.format("%strue\n", CTX_INSTALLJOB).getBytes(Charset.forName("UTF-8")));
            } else {
                fos.write(String.format("%sfalse\n", CTX_INSTALLJOB).getBytes(Charset.forName("UTF-8")));
            }
            if (!main.isEmpty()) {
                fos.write(String.format("%s%s\n", CTX_MAIN, main).getBytes(Charset.forName("UTF-8")));
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

    public boolean isJobInstalled() {
        return jobInstalled;
    }

    public void setJobInstalled(boolean jobInstalled) {
        this.jobInstalled = jobInstalled;
    }

    public String getMain() {
        return main;
    }

    public void setMain(String main) {
        this.main = main;
    }

    private void parse() throws IOException, SelfJarException {
        try {
            contextEntry.lockOut();
            List<String> lines = SelfUtils.readAllLines(contextEntry.getFile().toPath());
            for (String line : lines) {
                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                } else if (line.startsWith(CTX_COUNT)) {
                    exeCount = Integer.parseInt(line.substring(CTX_COUNT.length()));
                } else if (line.startsWith(CTX_ENVPARAM)) {
                    String[] splitted = line.substring(CTX_ENVPARAM.length()).split("=");
                    String key = splitted[0];
                    String value = "";
                    if (splitted.length > 1) {
                        value = splitted[1];
                    }
                    envEntries.put(key, value);
                } else if (line.startsWith(CTX_JOBPARAM)) {
                    String[] splitted = line.substring(CTX_JOBPARAM.length()).split("=");
                    String key = splitted[0];
                    String value = "";
                    if (splitted.length > 1) {
                        value = splitted[1];
                    }
                    jobParamsEntries.put(key, value);
                } else if (line.startsWith(CTX_INSTALLJOB)) {
                    if (line.substring(CTX_INSTALLJOB.length()).equals("true")) {
                        jobInstalled = true;
                    }
                } else if (line.startsWith(CTX_MAIN)) {
                    main = line.substring(CTX_MAIN.length());
                } else {
                    throw new SelfJarException("Invalid context entry: " + line);
                }
            }
        } finally {
            contextEntry.lockIn(contextEntry.getFile());
        }
    }

    @Override
    public String toString() {
        String ret = "=====================\n"
                + "|   [CONTEXT DATA]   |\n"
                + "=====================\n";
        ret += "I was executed " + exeCount + " times\n";

        if (!envEntries.isEmpty()) {
            ret += "There are environment variables:\n";
            for (Map.Entry<String, String> set : envEntries.entrySet()) {
                if (set.getValue().isEmpty()) {
                    ret += String.format("  %s\n", set.getKey());
                } else {
                    ret += String.format("  %s=%s\n", set.getKey(), set.getValue());
                }

            }
        }
        if (!jobParamsEntries.isEmpty()) {
            ret += "There are job parameters:\n";
            for (Map.Entry<String, String> set : jobParamsEntries.entrySet()) {
                if (set.getValue().isEmpty()) {
                    ret += String.format("  %s\n", set.getKey());
                } else {
                    ret += String.format("  %s=%s\n", set.getKey(), set.getValue());
                }
            }
        }
        if (jobInstalled) {
            ret += "A job is installed\n";
        }
        if (!main.isEmpty()) {
            ret += String.format("The main executable is %s\n", main);
        }
        return ret + "\n";
    }
}
