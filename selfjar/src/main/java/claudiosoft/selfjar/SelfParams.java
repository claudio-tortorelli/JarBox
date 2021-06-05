package claudiosoft.selfjar;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Claudio
 */
public class SelfParams {

    public static final String PARAM_PREFIX = "[sj]";

    public static final String INSTALL = "install";
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

    public static final String INSTALL_CLEAN = "clean";

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

    private List<String> jobArgs;

    public SelfParams() {
        this.del = new LinkedList<>();
        this.imp = new LinkedList<>();
        this.exp = "";
        this.delPar = new LinkedList<>();
        this.addPar = new LinkedList<>();
        this.delEnv = new LinkedList<>();
        this.addEnv = new LinkedList<>();
        this.jobArgs = new LinkedList<>();
        this.info = false;
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

}
