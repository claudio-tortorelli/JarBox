package claudiosoft.selfjar;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Claudio
 */
public class CoreEntries {

    private static final List<String> coreEntries = new LinkedList<>();

    static {
        coreEntries.add(".netbeans_automatic_build");
        coreEntries.add("META-INF/MANIFEST.MF");
        coreEntries.add("META-INF/maven/");
        coreEntries.add("META-INF/maven/claudiosoft/");
        coreEntries.add("META-INF/maven/claudiosoft/SelfGeneratingJar/");
        coreEntries.add("META-INF/maven/claudiosoft/SelfGeneratingJar/pom.properties");
        coreEntries.add("META-INF/maven/claudiosoft/SelfGeneratingJar/pom.xml");
        coreEntries.add("claudiosoft/selfgeneratingjar/");
        coreEntries.add("claudiosoft/selfgeneratingjar/BasicConsoleLogger$LogLevel.class");
        coreEntries.add("claudiosoft/selfgeneratingjar/BasicConsoleLogger.class");
        coreEntries.add("claudiosoft/selfgeneratingjar/Constants.class");
        coreEntries.add("claudiosoft/selfgeneratingjar/ContentEntry.class");
        coreEntries.add("claudiosoft/selfgeneratingjar/DaemonThread.class");
        coreEntries.add("claudiosoft/selfgeneratingjar/SelfJar.class");
        coreEntries.add("claudiosoft/selfgeneratingjar/SelfJarException.class");
        coreEntries.add("claudiosoft/selfgeneratingjar/Status$1.class");
        coreEntries.add("claudiosoft/selfgeneratingjar/Status$2.class");
        coreEntries.add("claudiosoft/selfgeneratingjar/Status.class");
        coreEntries.add("claudiosoft/selfgeneratingjar/Utils.class");
    }

    public static boolean isCore(String fullPath) {
        if (coreEntries.contains(fullPath)) {
            return true;
        }
        return false;
    }
}
