package claudiosoft.selfgeneratingjar;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 *
 * @author claudio.tortorelli
 */
public class SelfJar {

    private BasicConsoleLogger logger;
    private static final File FILE_LOCK = new File(System.getProperty("user.home") + File.separator + ".selfgenerating_lock");

    private JarContext context;
    private JarIdentity identity;
    private JarIO io;
    private JarContent content;

    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        BasicConsoleLogger logger = new BasicConsoleLogger(BasicConsoleLogger.LogLevel.DEBUG, "SelfJar");

        try {
            // avoid multiple instances
            Utils.testLockFile(FILE_LOCK);
            Utils.doLock(FILE_LOCK);
            logger.info("SelfJar started");
            if (args.length == 0) {
                ///////////////////////////////////////////////////////
                args = new String[20];
//            args[0] = "parent=c:\\canc\\metoo.jar";
                ///////////////////////////////////////////////////////
            }
            SelfJar selfJar = new SelfJar(args, logger);
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
            System.exit(Constants.RET_CODE_ERR);
        } finally {
            Utils.doUnlock();
        }
    }

    public SelfJar(String[] args, BasicConsoleLogger logger) throws URISyntaxException, IOException, InterruptedException, NoSuchAlgorithmException, SelfJarException {
        this.logger = logger;

        File selfJarFolder = null;
        try {
            identity = new JarIdentity();
            context = parseArgs(args);
            content = new JarContent(identity.getCurrentJar());
            io = new JarIO();
            logger.info(toString());
            // end initialization

            // TODO, use current date-time instead
            Random rnd = new Random();
            while (true) {
                selfJarFolder = new File(String.format("%s%s%s%d", System.getProperty("java.io.tmpdir"), File.separator, Constants.TMP_SELFJAR_FOLDER, rnd.nextInt(10000)));
                if (!selfJarFolder.exists()) {
                    break;
                }
            }
            selfJarFolder.mkdirs();

            io.out(content, selfJarFolder);

            File nextJar = io.in(selfJarFolder);
            int a = 0;
            //        if (JarIdentity.getIncarnationCount() < 3) {
            //            restartMySelf();
            //        }
        } finally {
            if (selfJarFolder != null) {
                Utils.deleteDirectory(selfJarFolder);
            }
        }
    }

    private void restartMySelf() throws IOException, InterruptedException {
        DaemonThread t1 = new DaemonThread("666", logger);
        t1.setDaemon(true);
        t1.start();
        t1.join();
        System.exit(0);
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        String ret = "\n";
        ret += identity.toString() + "\n";
        ret += context.toString() + "\n";
        ret += content.toString() + "\n";
        return ret;
    }

    public JarContext parseArgs(String[] args) throws SelfJarException {
        JarContext context = new JarContext();
        for (int iAr = 0; iAr < args.length; iAr++) {
            if (args[iAr] == null || args[iAr].isEmpty()) {
                continue;
            }
            String[] splitted = args[iAr].split("=");
            if (splitted.length != 2) {
                continue;
            }

            String param = splitted[0].toLowerCase().trim();
            String value = splitted[1];
            if (param.startsWith("parent")) {
                context.setParent(new File(value));
            } else if (param.startsWith("count")) {
                int counter = Integer.parseInt(value);
                context.setRebuildCount(counter);
            } else {
                throw new IllegalArgumentException("unrecognized input argument: " + param);
            }
        }
        return context;
    }

}
