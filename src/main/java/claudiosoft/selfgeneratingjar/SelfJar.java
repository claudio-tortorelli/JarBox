package claudiosoft.selfgeneratingjar;

import claudiosoft.selfgeneratingjar.Utils.OS;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author claudio.tortorelli
 */
public final class SelfJar {

    private BasicConsoleLogger logger;
    private static final File FILE_LOCK = new File(System.getProperty("user.home") + File.separator + ".selfgenerating_lock");

    private JarContext context;
    private JarIdentity identity;
    private JarIO io;
    private JarContent content;
    private boolean printInfo;

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
            content = new JarContent(identity.getCurrentJar());
            io = new JarIO();
            parseArgs(args);

            // use current date-time
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_SHORT);
            sdf.setTimeZone(Constants.DEFAULT_TIMEZONE);
            String dt = sdf.format(new Date());

            // create temp out folder
            selfJarFolder = new File(String.format("%s%s%s%s", System.getProperty("java.io.tmpdir"), File.separator, Constants.TMP_SELFJAR_FOLDER, dt));
            selfJarFolder.mkdirs();
            io.out(content, selfJarFolder);

            // update context file
            context = new JarContext(selfJarFolder);
            context.setExeCount(context.getExeCount() + 1);
            context.update();

            // end initialization
            if (printInfo()) {
                logger.info(toString());
                return; // no more to do
            }
            // start internal job
            // TODO
            // end internal job

            // create updated jar
            File nextJar = new File(String.format("%s%sselfJar%s.jar", System.getProperty("java.io.tmpdir"), File.separator, dt));
            io.in(selfJarFolder, nextJar);

            // start charun
            // it requires the context of parent jar (will be inherited by child)
            invokeCharun(identity.getCurrentJar().getAbsolutePath(), nextJar.getAbsolutePath());
        } finally {
            // unlock any open file
            io.closeAll(content);

            // remove temp folder
            if (selfJarFolder != null) {
                Utils.deleteDirectory(selfJarFolder);
            }
        }
    }

    /**
     * call charun
     *
     * @throws IOException
     * @throws InterruptedException
     */
    private void invokeCharun(String curJarPath, String nextJarPath) throws IOException, InterruptedException, SelfJarException {

        //TODO, call charun on right os platform
        OS os = Utils.getOperatingSystem();
        if (os.equals(OS.WINDOWS)) {

        } else if (os.equals(OS.OSX)) {

        } else if (os.equals(OS.LINUX)) {

        } else {
            throw new SelfJarException("unsupported os");
        }
        DaemonThread t1 = new DaemonThread("Charun", logger);
        t1.setDaemon(true);
        t1.start();
        //t1.join(); // not wait...
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
            if (param.startsWith("info")) {
                if (Boolean.parseBoolean(value)) {
                    setPrintInfo(true);
                }
            } else {
                // TODO, this will be updated when a job is defined
                throw new IllegalArgumentException("unrecognized input argument: " + param);
            }
        }
        return context;
    }

    public boolean printInfo() {
        return printInfo;
    }

    public void setPrintInfo(boolean printInfo) {
        this.printInfo = printInfo;
    }

}
