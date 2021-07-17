/**
 * JarBoxProject - https://github.com/claudio-tortorelli/JarBox/
 *
 * MIT License - 2021
 */
package claudiosoft.jarbox;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Main class of JarBox. It implements the execution flow, helps, logging and so
 * on.
 */
public final class JarBox {

    private static final File FILE_LOCK = new File(System.getProperty("user.home") + File.separator + ".selfgenerating_lock");

    private static BasicConsoleLogger logger = BasicConsoleLogger.get();

    private final String dateTime;
    private final File jarBoxTmpFolder;
    private final File jobZipFile;
    private final Content contentEntries;

    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        try {
            // avoid multiple instances
            Utils.testLockFile(FILE_LOCK);
            Utils.doLock(FILE_LOCK);
            new JarBox(args);
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
            System.exit(Constants.RET_CODE_ERR);
        } finally {
            Utils.doUnlock();
        }
    }

    public JarBox(String[] args) throws URISyntaxException, IOException, InterruptedException, NoSuchAlgorithmException, JarBoxException {
        try {
            Params.get().parseArgs(args);

            BasicConsoleLogger.get().info("JarBox started");

            SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_SHORT);
            sdf.setTimeZone(Constants.DEFAULT_TIMEZONE);
            dateTime = sdf.format(new Date());

            BasicConsoleLogger.get().debug("jar expanding...");
            this.jarBoxTmpFolder = new File(String.format("%s%s%s%s", System.getProperty("java.io.tmpdir"), File.separator, Constants.TMP_JARBOX_FOLDER, dateTime));
            this.jobZipFile = new File(String.format("%s%s%s", this.jarBoxTmpFolder, File.separator, Constants.JOB_ENTRY));
            this.contentEntries = new Content();
            if (Params.get().help()) {
                showHelp();
                return;
            }
            out();

            BasicConsoleLogger.get().debug("apply parameters...");
            applyParams();

            if (Params.get().info()) {
                BasicConsoleLogger.get().info(toString());
            }
            invokeJob();

            // create updated jar
            BasicConsoleLogger.get().debug("creating next jar...");
            File nextJar = in();

            //IO invoke charun to bring nextJar as current
            invokeCharun(nextJar.getAbsolutePath());
        } finally {
            // unlock any open file
            closeAll();
            BasicConsoleLogger.get().debug("closing");
        }
    }

    @Override
    public String toString() {
        String ret = "\n";
        try {
            ret += Identity.get().toString();
            ret += getContext().toString();
            ret += contentEntries.toString();
        } catch (Exception ex) {

        }
        return ret;
    }

    private void showHelp() {
        BasicConsoleLogger.get().info(Params.PARAM_PREFIX + "addpar=<param>, add a parameter to context. It will be passed to job by command line");
        BasicConsoleLogger.get().info(Params.PARAM_PREFIX + "delpar=<param>, delete a parameter from context");
        BasicConsoleLogger.get().info(Params.PARAM_PREFIX + "addenv=<variable>, add a system property to context. It will be set using -D to job");
        BasicConsoleLogger.get().info(Params.PARAM_PREFIX + "delenv=<variable>, delete a system property from context");
        BasicConsoleLogger.get().info(Params.PARAM_PREFIX + "install=<path/job.zip>, the path to job's archive to be installed");
        BasicConsoleLogger.get().info(Params.PARAM_PREFIX + "install=clean, delete the installed job");
        BasicConsoleLogger.get().info(Params.PARAM_PREFIX + "main=<job jar filename>, the workspace relative path to the executable job jar");
        BasicConsoleLogger.get().info(Params.PARAM_PREFIX + "import=<path/to/file>;<path/relative/workspace>;[true|false], import a external file in a workspace location, replacing if exists");
        BasicConsoleLogger.get().info(Params.PARAM_PREFIX + "export=<path/folder>, export the workspace to an external folder path");
        BasicConsoleLogger.get().info(Params.PARAM_PREFIX + "delete=<path/workspace/file>, remove a file from the internal workspace");
        BasicConsoleLogger.get().info(Params.PARAM_PREFIX + "loglevel=[debug|info|none], sets the console logger level. none is default");
        BasicConsoleLogger.get().info(Params.PARAM_PREFIX + "info=true, prints JarBox status and content to console");
        BasicConsoleLogger.get().info(Params.PARAM_PREFIX + "help=true, prints this help to console");
    }

    /**
     * Extract jar content to folder
     *
     * @throws IOException
     */
    private void out() throws IOException, JarBoxException {

        // build folder tree
        List<ContentEntry> content = this.contentEntries.getEntries();
        for (ContentEntry entry : content) {
            if (!entry.isDirectory()) {
                continue;
            }
            File folder = new File(String.format("%s%s%s", jarBoxTmpFolder, File.separator, entry.getFullName()));
            folder.mkdirs();
        }

        // export content to folders
        InputStream is = null;
        FileOutputStream fos = null;
        JarFile jar = new JarFile(Identity.get().currentJar());
        for (ContentEntry entry : content) {
            if (entry.isDirectory()) {
                continue;
            }
            try {
                is = jar.getInputStream(entry);
                File outFile = new File(jarBoxTmpFolder.getAbsoluteFile() + File.separator + entry.getName());
                fos = new FileOutputStream(outFile);
                Utils.inputToOutput(is, fos);
                // keep open entries in workspace
                if (!entry.getFullName().startsWith("job/workspace")) {
                    entry.lockIn(outFile);
                }
            } finally {
                Utils.closeQuietly(is);
                Utils.closeQuietly(fos);
            }
        }
    }

    /**
     * Create a jar from folder
     *
     * @return
     * @throws JarBoxException
     * @throws IOException
     *
     * https://stackoverflow.com/questions/1281229/how-to-use-jaroutputstream-to-create-a-jar-file
     */
    private File in() throws JarBoxException, IOException {
        JarOutputStream target = null;
        try {
            File nextJar = new File(String.format("%s%sjarbox%s.jar", System.getProperty("java.io.tmpdir"), File.separator, dateTime));
            target = new JarOutputStream(new FileOutputStream(nextJar));
            for (File nestedFile : jarBoxTmpFolder.listFiles()) {
                addToNextJar(jarBoxTmpFolder.getAbsolutePath(), nestedFile, target);
            }
            return nextJar;
        } finally {
            Utils.closeQuietly(target);
        }
    }

    private void closeAll() throws IOException, JarBoxException {
        List<ContentEntry> content = this.contentEntries.getEntries();
        for (ContentEntry entry : content) {
            if (entry.isDirectory()) {
                continue;
            }
            entry.lockOut();
        }
        Utils.deleteDirectory(jarBoxTmpFolder);
    }

    private void applyParams() throws JarBoxException, IOException {

        Params params = Params.get();
        Context context = getContext();

        try {
            context.applyParams();

            if (params.main() != null && !params.main().isEmpty()) {
                context.setMain(params.main());
            }
            if (params.install() != null && !params.install().isEmpty()) {
                ContentEntry entry = null;
                try {
                    entry = contentEntries.getEntry(Constants.JOB_ENTRY);
                } catch (JarBoxException ex) {
                    // not installed
                }
                if (entry != null && entry.isLocked()) {
                    entry.lockOut();
                }
                if (params.install().equals(Params.INSTALL_CLEAN)) {
                    jobZipFile.delete();
                    context.setJobInstalled(false);
                    context.setMain("");
                } else {
                    File jobZipToInstallFile = new File(params.install());
                    if (!jobZipToInstallFile.exists()) {
                        throw new JarBoxException("job archive not found at " + params.install());
                    }

                    // install job
                    File destJobFolder = new File(String.format("%s%s%s", jarBoxTmpFolder.getAbsolutePath(), File.separator, Constants.JOB_FOLDER));
                    destJobFolder.mkdirs();
                    File destJobFile = new File(String.format("%s%sjob.zip", destJobFolder.getAbsolutePath(), File.separator));
                    Files.copy(jobZipToInstallFile.toPath(), destJobFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    if (entry != null) {
                        entry.lockIn(jobZipToInstallFile);
                    }
                    context.setJobInstalled(true);
                }
            }
            for (String curRelPath : params.del()) {
                File toDel = new File(String.format("%s%s%s%s%s", jarBoxTmpFolder.getAbsolutePath(), File.separator, Constants.WS_ENTRY_FOLDER, File.separator, curRelPath));
                if (toDel.isFile()) {
                    toDel.delete();
                } else {
                    Utils.deleteDirectory(toDel);
                }
            }
            for (String value : params.imp()) {
                String[] data = value.split(";");
                if (data.length < 2 || data.length > 3) {
                    continue;
                }
                String externalPath = data[0].trim();
                String relativeWSPath = data[1].trim();
                boolean overwrite = true;
                if (data.length == 3) {
                    overwrite = data[2].trim().equalsIgnoreCase("true");
                }
                File src = new File(externalPath);
                if (!src.exists()) {
                    throw new JarBoxException(String.format("%s not found", src.getAbsolutePath()));
                }
                File dst = new File(String.format("%s%s%s%s%s", jarBoxTmpFolder.getAbsolutePath(), File.separator, Constants.WS_ENTRY_FOLDER, File.separator, relativeWSPath));
                if (!overwrite && dst.exists()) {
                    throw new JarBoxException(String.format("%s already exists", dst.getAbsolutePath()));
                }
                new File(dst.getParent()).mkdirs(); // create folders into ws
                Files.copy(src.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            if (!params.exp().isEmpty()) {
                File src = new File(String.format("%s%s%s", jarBoxTmpFolder.getAbsolutePath(), File.separator, Constants.WS_ENTRY_FOLDER));
                File dst = new File(params.exp());
                Utils.copyFolder(src, dst);
            }
        } finally {
            context.update();
        }
    }

    private String extractJob() throws JarBoxException, IOException {
        File jobDir = new File(String.format("%s%sjob%sdist", jarBoxTmpFolder, File.separator, File.separator));
        if (!jobDir.exists()) {
            jobDir.mkdirs();
        }

        String curJobFolder = jobDir.getAbsolutePath();

        ContentEntry entry = null;
        try {
            entry = contentEntries.getEntry(Constants.JOB_ENTRY);
        } catch (JarBoxException ex) {
            // not installed
        }
        if (entry != null && entry.isLocked()) {
            entry.lockOut();
        }

        byte[] buffer = new byte[Constants.BUFFER_SIZE];
        int len;
        FileInputStream fis = null;
        ZipInputStream zis = null;
        try {
            fis = new FileInputStream(jobZipFile);
            zis = new ZipInputStream(fis);

            ZipEntry zen = zis.getNextEntry();
            //we should check whether it is corrupted zip file or not. if corrupted then zip entry will be null
            while (zen != null) {
                String fileName = zen.getName();
                File newFile = new File(jobDir + File.separator + fileName);

                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(newFile);
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                } finally {
                    Utils.closeQuietly(fos);
                    zis.closeEntry();
                    zen = zis.getNextEntry();
                }
            }
        } finally {
            zis.closeEntry();
            Utils.closeQuietly(zis);
            Utils.closeQuietly(fis);

            if (entry != null) {
                entry.lockIn();
            }
        }
        return curJobFolder;
    }

    private Context getContext() throws JarBoxException, IOException {
        return new Context(contentEntries.getEntry(Constants.CONTEXT_ENTRY));
    }

    private void addToNextJar(String basePath, File source, JarOutputStream target) throws IOException, JarBoxException {

        String entryName = source.getPath().replace(basePath + File.separator, "");
        entryName = entryName.replace("\\", "/");

        BufferedInputStream in = null;
        try {
            if (source.isDirectory()) {
                if (!entryName.isEmpty()) {
                    if (!entryName.endsWith("/")) {
                        entryName += "/";
                    }
                    JarEntry entry = new JarEntry(entryName);
                    entry.setTime(source.lastModified());
                    target.putNextEntry(entry);
                    target.closeEntry();
                }
                for (File nestedFile : source.listFiles()) {
                    addToNextJar(basePath, nestedFile, target);
                }
                return;
            }

            // now unlock the entry...
            boolean newEntry = false;
            ContentEntry entry = null;
            try {
                entry = contentEntries.getEntry(entryName);
                entry.lockOut();
            } catch (JarBoxException ex) {
                // no entry yet
                newEntry = true;
            }

            if (newEntry) {
                // new entries allowed inside workspace or job.zip only
                if (!entryName.equals(Constants.JOB_ENTRY) && !entryName.startsWith(Constants.WS_ENTRY_FOLDER)) {
                    return;
                }
            }

            JarEntry jarEntry = new JarEntry(entryName);
            jarEntry.setTime(source.lastModified());
            target.putNextEntry(jarEntry);
            in = new BufferedInputStream(new FileInputStream(source));

            byte[] buffer = new byte[Constants.BUFFER_SIZE];
            while (true) {
                int count = in.read(buffer);
                if (count == -1) {
                    break;
                }
                target.write(buffer, 0, count);
            }
            target.closeEntry();
        } finally {
            Utils.closeQuietly(in);
        }
    }

    private void invokeJob() throws JarBoxException, IOException, InterruptedException {

        Context context = getContext();
        if (!context.isJobInstalled()) {
            return;
        }
        logger.info("start internal job");

        // extract the job archive
        String curJobFolder = extractJob();

        // create params list
        LinkedList<String> pbArgs = new LinkedList<>();
        if (context.getMain().toLowerCase().endsWith(".jar")) {
            pbArgs.add("java");
        }

        // apply system properties
        for (Map.Entry<String, String> set : context.getEnvEntries().entrySet()) {
            String env = set.getKey();
            if (!set.getValue().isEmpty()) {
                env = String.format("%s=%s", set.getKey(), set.getValue());
            }
            pbArgs.add(String.format("-D%s", env));
        }

        pbArgs.add("-jar");
        String jobJar = String.format("%s%s%s", curJobFolder, File.separator, context.getMain());
        logger.info("job jar is " + jobJar);
        pbArgs.add(jobJar);

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
     * @param nextJarPath
     * @throws IOException
     * @throws InterruptedException
     * @throws JarBoxException
     */
    private void invokeCharun(String nextJarPath) throws IOException, InterruptedException, JarBoxException {

        File foo = File.createTempFile("foo", ".tmp");
        String parentFolder = foo.getParent();
        foo.delete();

        File charunOutFile = null;
        File charunInFile = null;

        logger.debug("starting Charun...");
        //call charun on right  platform
        Utils.OS os = Utils.getOperatingSystem();
        if (os.equals(Utils.OS.WINDOWS)) {
            charunInFile = Utils.getFileFromRes("charun/win/charun.exe");
            charunOutFile = new File(String.format("%s%scharun.exe", parentFolder, File.separator));
        } else if (os.equals(Utils.OS.OSX)) {
            //TODO...rebuild charun
        } else if (os.equals(Utils.OS.LINUX)) {
            if (!System.getProperty("os.arch").equals("x86")) {
                charunInFile = Utils.getFileFromRes("charun/linux/charunX64");
                charunOutFile = new File(String.format("%s%scharunX64", parentFolder, File.separator));
            } else {
                //TODO
            }
        }
        if (charunInFile == null || !charunInFile.exists()) {
            throw new JarBoxException("unsupported os");
        }

        Files.copy(charunInFile.toPath(), charunOutFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        LinkedList<String> pbArgs = new LinkedList<>();
        pbArgs.add(charunOutFile.getAbsolutePath());
        pbArgs.add(nextJarPath);
        pbArgs.add(Identity.get().currentJar().getAbsolutePath());
        if (Params.get().info()) {
            pbArgs.add("-verbose");
        }

        ProcessBuilder processBuilder = new ProcessBuilder(pbArgs);
        Process insideProc = processBuilder.start();

        // print charun outputs
        Utils.inheritIO(insideProc.getInputStream(), System.out);
        Utils.inheritIO(insideProc.getErrorStream(), System.err);

        insideProc.waitFor();
    }

}
