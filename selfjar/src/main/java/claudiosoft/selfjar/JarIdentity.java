package claudiosoft.selfjar;

import claudiosoft.selfjar.commons.SelfJarException;
import java.io.File;
import java.io.FilenameFilter;

/**
 *
 * @author Claudio
 */
public class JarIdentity {

    private final String fullName;
    private final String className;
    private final String parentFolderJar;
    private final String jarName;
    private final File currentJar;
    private final String javaRuntime;

    private final Class MAIN_CLASS = SelfJar.class;
    private final String VERSION = "1.0.0";

    public JarIdentity() throws SelfJarException {

        try {
            this.fullName = MAIN_CLASS.getName();
            this.className = MAIN_CLASS.getSimpleName();
            this.parentFolderJar = new File(MAIN_CLASS.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getAbsolutePath();
            String curName = new File(MAIN_CLASS.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getName();
            if (curName.equals("classes")) {
                String[] list = new File(getParentFolderJar()).list(new FilenameFilter() {
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
            this.currentJar = new File(getParentFolderJar() + File.separator + getJarName());
            this.javaRuntime = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        } catch (Exception ex) {
            // wrap for convenience
            throw new SelfJarException(ex.getMessage(), ex);
        }
    }

    public String getFullName() {
        return fullName;
    }

    public String getClassName() {
        return className;
    }

    public String getParentFolderJar() {
        return parentFolderJar;
    }

    public String getJarName() {
        return jarName;
    }

    public File getCurrentJar() {
        return currentJar;
    }

    public String getJavaRuntime() {
        return javaRuntime;
    }

    public String getVersion() {
        return VERSION;
    }

    @Override
    public String toString() {
        String ret = "--- Jar Identity ---\n";
        ret += "My version is " + getVersion() + "\n";
        ret += "My full name is " + getFullName() + "\n";
        ret += "My simple name is " + getClassName() + "\n";
        ret += "I'm contained into " + getCurrentJar().getAbsolutePath() + "\n";
        ret += "I'm executing by JVM " + getJavaRuntime() + "\n";
        return ret;
    }
}
