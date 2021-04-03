package claudiosoft.selfjar;

import claudiosoft.selfjar.BasicConsoleLogger.LogLevel;
import claudiosoft.selfjar.SelfUtils.OS;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;

/**
 *
 * @author claudio.tortorelli
 */
public final class SelfJar {

    private BasicConsoleLogger logger;
    private static final File FILE_LOCK = new File(System.getProperty("user.home") + File.separator + ".selfgenerating_lock");

    private SelfParams params;

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

        try {
            //////////// start initialization
            parseArgs(args);

            logger.debug("jar expanding...");
            IO.get().out();

            // update context file
            logger.debug("context updating...");

            //TODO, update con il params
            Context context = IO.get().getContext();
            context.setExeCount(context.getExeCount() + 1);
            context.update();
            /////////// end initialization

            if (params.info()) {
                logger.info(toString());
            }
            // start internal job
            logger.info("start internal job");
            // TODO
            logger.info("end internal job");
            // end internal job

            // create updated jar
            logger.debug("creating next jar...");
            File nextJar = IO.get().in();

            // invoke charun to bring nextJar as current
            invokeCharun(nextJar.getAbsolutePath());
        } finally {
            // unlock any open file
            IO.get().closeAll();
            logger.debug("closing");
        }
    }

    /**
     * call charun
     *
     * @throws IOException
     * @throws InterruptedException
     */
    private void invokeCharun(String nextJarPath) throws IOException, InterruptedException, SelfJarException {

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
        pbArgs.add(Identity.get().currentJar().getAbsolutePath());

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
        try {
            ret += Identity.get().toString() + "\n";
            ret += IO.get().toString() + "\n";
        } catch (SelfJarException ex) {

        }
        return ret;
    }

    public void parseArgs(String[] args) throws SelfJarException {
        params = new SelfParams();

        for (int iAr = 0; iAr < args.length; iAr++) {
            if (args[iAr] == null || args[iAr].isEmpty()) {
                continue;
            }

            String[] splitted = args[iAr].split("=");
            if (splitted.length == 2) {
                String param = splitted[0].toLowerCase().trim();
                if (param.startsWith(SelfConstants.PARAM_PREFIX)) {
                    param = param.substring(SelfConstants.PARAM_PREFIX.length());
                    String value = splitted[1];
                    if (param.startsWith(SelfParams.INFO) && value.equalsIgnoreCase("true")) {
                        // enable internal info printing
                        params.setPrintInfo(true);
                    } else if (param.startsWith("loglevel")) {
                        // set logger level
                        if (value.equalsIgnoreCase("debug")) {
                            logger = new BasicConsoleLogger(LogLevel.DEBUG, SelfConstants.LOGGER_NAME);
                        } else if (value.equalsIgnoreCase("info")) {
                            logger = new BasicConsoleLogger(LogLevel.NORMAL, SelfConstants.LOGGER_NAME);
                        } else {
                            logger = new BasicConsoleLogger(LogLevel.NONE, SelfConstants.LOGGER_NAME);
                        }
                    } else if (param.startsWith(SelfParams.INSTALL)) {
                        // install or remove a job
                        params.setInstall(value);
                    } else if (param.startsWith(SelfParams.MAIN)) {
                        // add a job main executable to context
                        params.setMain(value);
                    } else if (param.startsWith(SelfParams.ADDENV)) {
                        // add an environment variable to context
                        params.addEnv().add(value);
                    } else if (param.startsWith(SelfParams.DELENV)) {
                        // delete an environment variable to context
                        params.delEnv().add(value);
                    } else if (param.startsWith(SelfParams.ADDPAR)) {
                        // add a job parameter to context
                        params.addPar().add(value);
                    } else if (param.startsWith(SelfParams.DELPAR)) {
                        // delete a job parameter to context
                        params.delPar().add(value);
                    } else if (param.startsWith(SelfParams.EXP)) {
                        // export workspace to folder
                        params.exp().add(value);
                    } else if (param.startsWith(SelfParams.IMP)) {
                        // import file into workspace
                        params.imp().add(value);
                    } else if (param.startsWith(SelfParams.DEL)) {
                        // delete file from workspace
                        params.del().add(value);
                    } else {
                        params.jobArgs().add(args[iAr]);
                    }
                } else {
                    params.jobArgs().add(args[iAr]);
                }
            } else {
                params.jobArgs().add(args[iAr]);
            }
        }
    }
}
