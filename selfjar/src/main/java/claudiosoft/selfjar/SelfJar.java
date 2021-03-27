package claudiosoft.selfjar;

import claudiosoft.selfjar.commons.SelfConstants;
import claudiosoft.selfjar.commons.SelfJarException;
import claudiosoft.selfjar.commons.SelfUtils;
import claudiosoft.selfjar.commons.SelfUtils.OS;
import claudiosoft.selfjar.BasicConsoleLogger.LogLevel;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

/**
 *
 * @author claudio.tortorelli
 */
public final class SelfJar {

    private BasicConsoleLogger logger;
    private static final File FILE_LOCK = new File(System.getProperty("user.home") + File.separator + ".selfgenerating_lock");

    private JarIdentity identity;
    private JarIO io;
    private JarContent content;
    private boolean printInfo;

    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        BasicConsoleLogger logger = new BasicConsoleLogger(BasicConsoleLogger.LogLevel.NONE, "SelfJar");
        try {
            // avoid multiple instances
            SelfUtils.testLockFile(FILE_LOCK);
            SelfUtils.doLock(FILE_LOCK);
            logger.info("SelfJar started");
            SelfJar selfJar = new SelfJar(args, logger);
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
            System.exit(SelfConstants.RET_CODE_ERR);
        } finally {
            SelfUtils.doUnlock();
        }
    }

    public SelfJar(String[] args, BasicConsoleLogger logger) throws URISyntaxException, IOException, InterruptedException, NoSuchAlgorithmException, SelfJarException {
        this.logger = logger;

        File selfJarFolder = null;
        File nextJar = null;
        try {
            identity = new JarIdentity();
            content = new JarContent(identity.getCurrentJar());
            io = new JarIO();
            parseArgs(args);

            // use current date-time
            SimpleDateFormat sdf = new SimpleDateFormat(SelfConstants.DATE_FORMAT_SHORT);
            sdf.setTimeZone(SelfConstants.DEFAULT_TIMEZONE);
            String dt = sdf.format(new Date());

            // create temp out folder
            selfJarFolder = new File(String.format("%s%s%s%s", System.getProperty("java.io.tmpdir"), File.separator, SelfConstants.TMP_SELFJAR_FOLDER, dt));
            selfJarFolder.mkdirs();
            logger.debug("jar expanding...");
            io.out(content, selfJarFolder);

            // update context file
            logger.debug("context updating...");
            JarContext context = io.getContext();
            context.setExeCount(context.getExeCount() + 1);
            io.updateContext();
            // end initialization

            if (printInfo()) {
                logger.info(toString());
            }

            // start internal job
            logger.info("start internal job");
            // TODO
            logger.info("end internal job");
            // end internal job
            io.closeAll(content);

            // create updated jar
            logger.debug("creating next jar...");
            nextJar = new File(String.format("%s%sselfJar%s.jar", System.getProperty("java.io.tmpdir"), File.separator, dt));
            io.in(selfJarFolder, nextJar);

            invokeCharun(identity.getCurrentJar().getAbsolutePath(), nextJar.getAbsolutePath());
        } finally {
            // unlock any open file
            io.closeAll(content);

            // remove temp folder
            if (selfJarFolder != null) {
                SelfUtils.deleteDirectory(selfJarFolder);
            }
            logger.debug("closing");
        }
    }

    /**
     * call charun
     *
     * @throws IOException
     * @throws InterruptedException
     */
    private void invokeCharun(String curJarPath, String nextJarPath) throws IOException, InterruptedException, SelfJarException {

        File foo = File.createTempFile("foo", ".tmp");
        File charunOutFile = null;
        File charunInFile = null;

        logger.debug("starting Charun...");
        //call charun on right  platform
        OS os = SelfUtils.getOperatingSystem();
        if (os.equals(OS.WINDOWS)) {
            charunInFile = SelfUtils.getFileFromRes("charun/win/Charun.exe");
            charunOutFile = new File(String.format("%s%sCharun.exe", foo.getParent(), File.separator));
            foo.delete();
        } else if (os.equals(OS.OSX)) {
            foo.delete();
        } else if (os.equals(OS.LINUX)) {
            foo.delete();
        } else {
            throw new SelfJarException("unsupported os");
        }

        Files.copy(charunInFile.toPath(), charunOutFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        LinkedList<String> pbArgs = new LinkedList<>();
        pbArgs.add(charunOutFile.getAbsolutePath());
        pbArgs.add(nextJarPath);
        pbArgs.add(curJarPath);

        ProcessBuilder processBuilder = new ProcessBuilder(pbArgs);
        Process insideProc = processBuilder.start();

        // print charun output
        SelfUtils.inheritIO(insideProc.getInputStream(), System.out);
        SelfUtils.inheritIO(insideProc.getErrorStream(), System.err);
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        String ret = "\n";
        ret += identity.toString() + "\n";
        ret += io.getContext().toString() + "\n";
        ret += content.toString() + "\n";
        return ret;
    }

    public void parseArgs(String[] args) throws SelfJarException {

        for (int iAr = 0; iAr < args.length; iAr++) {
            if (args[iAr] == null || args[iAr].isEmpty()) {
                continue;
            }
            String[] splitted = args[iAr].split("=");
            if (splitted.length > 2) {
                continue;
            }

            String param = splitted[0].toLowerCase().trim();
            String value = ""; // the switch argument may be present
            if (splitted.length == 2) {
                value = splitted[1];
            }
            if (param.startsWith("info")) {
                setPrintInfo(true);
            } else if (param.startsWith("loglevel")) {
                if (value.equalsIgnoreCase("debug")) {
                    logger = new BasicConsoleLogger(LogLevel.DEBUG, "SelfJar");
                } else if (value.equalsIgnoreCase("info")) {
                    logger = new BasicConsoleLogger(LogLevel.NORMAL, "SelfJar");
                } else {
                    logger = new BasicConsoleLogger(LogLevel.NONE, "SelfJar");
                }
            } else {
                // TODO, this will be updated when a job is defined
                throw new IllegalArgumentException("unrecognized input argument: " + param);
            }
        }
    }

    public boolean printInfo() {
        return printInfo;
    }

    public void setPrintInfo(boolean printInfo) {
        this.printInfo = printInfo;
    }

}
