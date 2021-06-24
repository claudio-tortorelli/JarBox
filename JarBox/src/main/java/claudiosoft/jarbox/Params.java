package claudiosoft.jarbox;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Claudio
 */
public class Params {

    public static final String PARAM_PREFIX = "[sj]";

    public static final String INSTALL = "install";
    public static final String INSTALL_CLEAN = "clean";
    public static final String MAIN = "main";
    public static final String ADDENV = "addenv";
    public static final String DELENV = "delenv";
    public static final String ADDPAR = "addpar";
    public static final String DELPAR = "delpar";
    public static final String EXP = "export";
    public static final String IMP = "import";
    public static final String DEL = "delete";
    public static final String INFO = "info";
    public static final String LOGLEVEL = "loglevel";
    public static final String HELP = "help";

    private String install;
    private String main;
    private List<String> addEnv;
    private List<String> delEnv;
    private List<String> addPar;
    private List<String> delPar;
    private String exp;
    private List<String> imp;
    private List<String> del;
    private boolean info;
    private boolean help;

    private List<String> jobArgs;

    private static Params params = null;

    public static Params get() throws JarBoxException {
        if (params != null) {
            return params;
        }
        params = new Params();
        return params;
    }

    private Params() {
        this.del = new LinkedList<>();
        this.imp = new LinkedList<>();
        this.exp = "";
        this.delPar = new LinkedList<>();
        this.addPar = new LinkedList<>();
        this.delEnv = new LinkedList<>();
        this.addEnv = new LinkedList<>();
        this.jobArgs = new LinkedList<>();
        this.info = false;
        this.help = false;
    }

    public String install() {
        return install;
    }

    public void install(String install) {
        this.install = install;
    }

    public String main() {
        return main;
    }

    public void main(String main) {
        this.main = main;
    }

    public List<String> addEnv() {
        return addEnv;
    }

    public List<String> delEnv() {
        return delEnv;
    }

    public List<String> addPar() {
        return addPar;
    }

    public List<String> delPar() {
        return delPar;
    }

    public String exp() {
        return exp;
    }

    public void exp(String exportDir) {
        exp = exportDir;
    }

    public List<String> imp() {
        return imp;
    }

    public List<String> del() {
        return del;
    }

    public List<String> jobArgs() {
        return jobArgs;
    }

    public boolean info() {
        return info;
    }

    public void info(boolean info) {
        this.info = info;
    }

    public boolean help() {
        return help;
    }

    public void help(boolean help) {
        this.help = help;
    }

    public void parseArgs(String[] args) throws JarBoxException {
        boolean loggerCreated = false;
        for (int iAr = 0; iAr < args.length; iAr++) {
            if (args[iAr] == null || args[iAr].isEmpty()) {
                continue;
            }
            String[] splitted = args[iAr].split("=");
            if (splitted.length != 2 && splitted.length != 3) {
                params.jobArgs().add(args[iAr]);
                continue;
            }
            String param = splitted[0].toLowerCase().trim();
            if (!param.startsWith(Params.PARAM_PREFIX)) {
                params.jobArgs().add(args[iAr]);
                continue;
            }
            param = param.substring(Params.PARAM_PREFIX.length());
            String value = splitted[1];
            if (splitted.length > 2 && !splitted[2].isEmpty()) {
                value = String.format("%s=%s", splitted[1], splitted[2]);
            }

            if (param.startsWith(Params.INFO) && value.equalsIgnoreCase("true")) {
                params.info(true);// enable internal info printing
            } else if (param.startsWith(Params.HELP) && value.equalsIgnoreCase("true")) {
                params.help(true);
            } else if (param.startsWith(Params.LOGLEVEL)) {
                loggerCreated = true;
                // set logger level
                if (value.equalsIgnoreCase("debug")) {
                    BasicConsoleLogger.get(BasicConsoleLogger.LogLevel.DEBUG, Constants.LOGGER_NAME);
                } else if (value.equalsIgnoreCase("info")) {
                    BasicConsoleLogger.get(BasicConsoleLogger.LogLevel.NORMAL, Constants.LOGGER_NAME);
                } else {
                    BasicConsoleLogger.get(BasicConsoleLogger.LogLevel.NONE, Constants.LOGGER_NAME);
                }
            } else if (param.startsWith(Params.INSTALL)) {
                // install or remove a job
                params.install(value);
            } else if (param.startsWith(Params.MAIN)) {
                // add a job main executable to context
                params.main(value);
            } else if (param.startsWith(Params.ADDENV)) {
                // add an environment variable to context
                params.addEnv().add(value);
            } else if (param.startsWith(Params.DELENV)) {
                // delete an environment variable to context
                params.delEnv().add(value);
            } else if (param.startsWith(Params.ADDPAR)) {
                // add a job parameter to context
                params.addPar().add(value);
            } else if (param.startsWith(Params.DELPAR)) {
                // delete a job parameter to context
                params.delPar().add(value);
            } else if (param.startsWith(Params.EXP)) {
                // export workspace to folder
                params.exp(value);
            } else if (param.startsWith(Params.IMP)) {
                // import file into workspace
                params.imp().add(value);
            } else if (param.startsWith(Params.DEL)) {
                // delete file from workspace
                params.del().add(value);
            } else {
                throw new JarBoxException("Invalid self jar parameter: " + param);
            }
        }
        if (!loggerCreated) {
            BasicConsoleLogger.get(BasicConsoleLogger.LogLevel.NONE, Constants.LOGGER_NAME); // default
        }
    }

}
