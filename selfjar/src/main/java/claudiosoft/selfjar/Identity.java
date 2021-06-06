package claudiosoft.selfjar;

import java.io.File;
import java.io.FilenameFilter;

/**
 *
 * @author Claudio
 */
public class Identity {

    private final String fullName;
    private final String className;
    private final String parentFolderJar;
    private final String jarName;
    private final File currentJar;
    private final String javaRuntime;

    private final Class MAIN_CLASS = SelfJar.class;
    private final String VERSION;

    private static Identity identity = null;

    public static Identity get() throws SelfJarException {
        if (identity != null) {
            return identity;
        }
        identity = new Identity();
        return identity;
    }

    private Identity() throws SelfJarException {

        try {
            this.VERSION = MAIN_CLASS.getPackage().getImplementationVersion();
            this.fullName = MAIN_CLASS.getName();
            this.className = MAIN_CLASS.getSimpleName();
            this.parentFolderJar = new File(MAIN_CLASS.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getAbsolutePath();
            String curName = new File(MAIN_CLASS.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getName();
            if (curName.equals("classes")) {
                String[] list = new File(parentFolderJar()).list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.toLowerCase().endsWith(".jar");
                    }
                });
                if (list.length > 0) {
                    curName = list[0];
                }
            }
            this.jarName = curName;
            this.currentJar = new File(parentFolderJar() + File.separator + jarName());
            this.javaRuntime = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        } catch (Exception ex) {
            // wrap for convenience
            throw new SelfJarException(ex.getMessage(), ex);
        }
    }

    public String fullName() {
        return fullName;
    }

    public String className() {
        return className;
    }

    public String parentFolderJar() {
        return parentFolderJar;
    }

    public String jarName() {
        return jarName;
    }

    public File currentJar() {
        return currentJar;
    }

    public String javaRuntime() {
        return javaRuntime;
    }

    public String version() {
        return VERSION;
    }

    @Override
    public String toString() {
        String ret = "==================\n"
                + "|   [IDENTITY]   |\n"
                + "==================\n";
        ret += "My version is " + version() + "\n";
        ret += "My full name is " + fullName() + "\n";
        ret += "My simple name is " + className() + "\n";
        ret += "I'm contained into " + currentJar().getAbsolutePath() + "\n";
        ret += "I'm executing by JVM " + javaRuntime() + "\n";
        return ret + "\n";
    }
}
