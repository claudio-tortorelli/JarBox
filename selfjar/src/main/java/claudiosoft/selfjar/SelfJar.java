package claudiosoft.selfjar;

import claudiosoft.selfjar.BasicConsoleLogger.LogLevel;
import claudiosoft.selfjar.Utils.OS;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author claudio.tortorelli
 */
public final class SelfJar {

    private BasicConsoleLogger logger;
    private static final File FILE_LOCK = new File(System.getProperty("user.home") + File.separator + ".selfgenerating_lock");

    private Params params;

    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        try {
            // avoid multiple instances
            Utils.testLockFile(FILE_LOCK);
            Utils.doLock(FILE_LOCK);
            new SelfJar(args);
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
            System.exit(Constants.RET_CODE_ERR);
        } finally {
            Utils.doUnlock();
        }
    }

    public SelfJar(String[] args) throws URISyntaxException, IOException, InterruptedException, NoSuchAlgorithmException, SelfJarException {

        try {
            //////////// start initialization
            parseArgs(args);
            logger.info("SelfJar started");

            logger.debug("jar expanding...");
            IO.get().out();

            logger.debug("apply parameters...");
            IO.get().applyParams(params);
            /////////// end initialization

            if (params.info()) {
                logger.info(toString());
            }

            invokeJob();

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

    private void invokeJob() throws SelfJarException, IOException, InterruptedException {

        Context context = IO.get().getContext();
        if (!context.isJobInstalled()) {
            return;
        }
        logger.info("start internal job");

        // extract the job archive
        String curJobFolder = IO.get().extractJob();

        // create params list
        LinkedList<String> pbArgs = new LinkedList<>();
        if (context.getMain().toLowerCase().endsWith(".jar")) {
            pbArgs.add("java");
            pbArgs.add("-jar");
        }

        // apply environment properties
        for (Map.Entry<String, String> set : context.getEnvEntries().entrySet()) {
            String env = set.getKey();
            if (!set.getValue().isEmpty()) {
                env = String.format("%s=%s", set.getKey(), set.getValue());
            }
            pbArgs.add(String.format("-D%s", env));
        }

        pbArgs.add(String.format("%s%s%s", curJobFolder, File.separator, context.getMain()));

        for (Map.Entry<String, String> set : context.getJobParamsEntries().entrySet()) {
            if (set.getValue().isEmpty()) {
                pbArgs.add(String.format("%s", set.getKey()));
            } else {
                pbArgs.add(String.format("%s=%s", set.getKey(), set.getValue()));
            }
        }

        ProcessBuilder processBuilder = new ProcessBuilder(pbArgs);
        Process insideProc = processBuilder.start();

        // print job outputs
        Utils.inheritIO(insideProc.getInputStream(), System.out);
        Utils.inheritIO(insideProc.getErrorStream(), System.err);

        insideProc.waitFor();

        logger.info("end internal job");
    }

    /**
     * call charun
     *
     * @throws IOException
     * @throws InterruptedException
     */
    private void invokeCharun(String nextJarPath) throws IOException, InterruptedException, SelfJarException {

        File foo = File.createTempFile("foo", ".tmp");
        String parentFolder = foo.getParent();
        foo.delete();

        File charunOutFile = null;
        File charunInFile = null;

        logger.debug("starting Charun...");
        //call charun on right  platform
        OS os = Utils.getOperatingSystem();
        if (os.equals(OS.WINDOWS)) {
            charunInFile = Utils.getFileFromRes("charun/win/Charun.exe");
            charunOutFile = new File(String.format("%s%sCharun.exe", parentFolder, File.separator));
        } else if (os.equals(OS.OSX)) {
            //TODO...rebuild charun
        } else if (os.equals(OS.LINUX)) {
            if (!System.getProperty("os.arch").equals("x86")) {
                charunInFile = Utils.getFileFromRes("charun/linux/CharunX64");
                charunOutFile = new File(String.format("%s%sCharunX64", parentFolder, File.separator));
            } else {
                //TODO
            }
        }
        if (charunInFile == null || !charunInFile.exists()) {
            throw new SelfJarException("unsupported os");
        }

        Files.copy(charunInFile.toPath(), charunOutFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        LinkedList<String> pbArgs = new LinkedList<>();
        pbArgs.add(charunOutFile.getAbsolutePath());
        pbArgs.add(nextJarPath);
        pbArgs.add(Identity.get().currentJar().getAbsolutePath());
        if (params.info()) {
            pbArgs.add("-verbose");
        }

        ProcessBuilder processBuilder = new ProcessBuilder(pbArgs);
        Process insideProc = processBuilder.start();

        // print charun outputs
        Utils.inheritIO(insideProc.getInputStream(), System.out);
        Utils.inheritIO(insideProc.getErrorStream(), System.err);

        insideProc.waitFor();
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        String ret = "\n";
        try {
            ret += Identity.get().toString();
            ret += IO.get().toString();
        } catch (SelfJarException ex) {

        }
        return ret;
    }

    public void parseArgs(String[] args) throws SelfJarException {
        params = new Params();

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
                continue;
            } else if (param.startsWith(Params.LOGLEVEL)) {
                // set logger level
                if (value.equalsIgnoreCase("debug")) {
                    logger = BasicConsoleLogger.get(LogLevel.DEBUG, Constants.LOGGER_NAME);
                } else if (value.equalsIgnoreCase("info")) {
                    logger = BasicConsoleLogger.get(LogLevel.NORMAL, Constants.LOGGER_NAME);
                } else {
                    logger = BasicConsoleLogger.get(LogLevel.NONE, Constants.LOGGER_NAME);
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
                throw new SelfJarException("Invalid self jar parameter: " + param);
            }
        }
        if (logger == null) {
            logger = BasicConsoleLogger.get(LogLevel.NONE, Constants.LOGGER_NAME); // default
        }
    }
}
