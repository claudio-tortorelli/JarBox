package claudiosoft.selfjar;

import claudiosoft.selfjar.Utils.OS;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
 *
 * @author claudio.tortorelli
 */
public class IO {

    private final File selfJarTmpFolder;
    private final File jobZipFile;
    private final File nextJar;
    private final Content contentEntries;

    private static BasicConsoleLogger logger = BasicConsoleLogger.get();

    private static IO io = null;

    public static IO get() throws SelfJarException {
        if (io != null) {
            return io;
        }
        io = new IO();
        return io;
    }

    private IO() throws SelfJarException {
        // use current date-time
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_SHORT);
        sdf.setTimeZone(Constants.DEFAULT_TIMEZONE);
        String dateTime = sdf.format(new Date());

        // create temp out folder
        this.selfJarTmpFolder = new File(String.format("%s%s%s%s", System.getProperty("java.io.tmpdir"), File.separator, Constants.TMP_SELFJAR_FOLDER, dateTime));
        this.jobZipFile = new File(String.format("%s%s%s", this.selfJarTmpFolder, File.separator, Constants.JOB_ENTRY));
        this.nextJar = new File(String.format("%s%sselfJar%s.jar", System.getProperty("java.io.tmpdir"), File.separator, dateTime));
        this.contentEntries = new Content();
    }

    /**
     * Extract jar content to folder
     *
     * @throws IOException
     */
    public void out() throws IOException, SelfJarException {

        // build folder tree
        List<ContentEntry> content = this.contentEntries.getContentEntries();
        for (ContentEntry entry : content) {
            if (!entry.isDirectory()) {
                continue;
            }
            File folder = new File(String.format("%s%s%s", selfJarTmpFolder, File.separator, entry.getFullName()));
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
                File outFile = new File(selfJarTmpFolder.getAbsoluteFile() + File.separator + entry.getName());
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
     * @throws SelfJarException
     * @throws IOException
     *
     * https://stackoverflow.com/questions/1281229/how-to-use-jaroutputstream-to-create-a-jar-file
     */
    public File in() throws SelfJarException, IOException {
        JarOutputStream target = null;
        try {
            target = new JarOutputStream(new FileOutputStream(nextJar));
            for (File nestedFile : selfJarTmpFolder.listFiles()) {
                addToNextJar(selfJarTmpFolder.getAbsolutePath(), nestedFile, target);
            }
        } finally {
            Utils.closeQuietly(target);
        }
        return nextJar;
    }

    public void closeAll() throws IOException, SelfJarException {
        List<ContentEntry> content = this.contentEntries.getContentEntries();
        for (ContentEntry entry : content) {
            if (entry.isDirectory()) {
                continue;
            }
            entry.lockOut();
        }
        Utils.deleteDirectory(selfJarTmpFolder);
    }

    public Context getContext() throws SelfJarException, IOException {
        return new Context(contentEntries.getContext());
    }

    @Override
    public String toString() {
        String ret = "";
        try {
            ret += getContext().toString();
            ret += contentEntries.toString();

        } catch (Exception ex) {

        }
        return ret;
    }

    public void applyParams() throws SelfJarException, IOException {

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
                    entry = contentEntries.getContentEntry(Constants.JOB_ENTRY);
                } catch (SelfJarException ex) {
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
                        throw new SelfJarException("job archive not found at " + params.install());
                    }

                    // install job
                    File destJobFolder = new File(String.format("%s%s%s", selfJarTmpFolder.getAbsolutePath(), File.separator, Constants.JOB_FOLDER));
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
                File toDel = new File(String.format("%s%s%s%s%s", selfJarTmpFolder.getAbsolutePath(), File.separator, Constants.WS_ENTRY_FOLDER, File.separator, curRelPath));
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
                    throw new SelfJarException(String.format("%s not found", src.getAbsolutePath()));
                }
                File dst = new File(String.format("%s%s%s%s%s", selfJarTmpFolder.getAbsolutePath(), File.separator, Constants.WS_ENTRY_FOLDER, File.separator, relativeWSPath));
                if (!overwrite && dst.exists()) {
                    throw new SelfJarException(String.format("%s already exists", dst.getAbsolutePath()));
                }
                new File(dst.getParent()).mkdirs(); // create folders into ws
                Files.copy(src.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            if (!params.exp().isEmpty()) {
                File src = new File(String.format("%s%s%s", selfJarTmpFolder.getAbsolutePath(), File.separator, Constants.WS_ENTRY_FOLDER));
                File dst = new File(params.exp());
                Utils.copyFolder(src, dst);
            }
        } finally {
            context.update();
        }
    }

    public String extractJob() throws SelfJarException, IOException {
        File jobDir = new File(String.format("%s%sjob%sjob", selfJarTmpFolder, File.separator, File.separator));
        if (!jobDir.exists()) {
            jobDir.mkdirs();
        }

        String curJobFolder = jobDir.getAbsolutePath();

        ContentEntry entry = null;
        try {
            entry = contentEntries.getContentEntry(Constants.JOB_ENTRY);
        } catch (SelfJarException ex) {
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

    private void addToNextJar(String basePath, File source, JarOutputStream target) throws IOException, SelfJarException {

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
                entry = contentEntries.getContentEntry(entryName);
                entry.lockOut();
            } catch (SelfJarException ex) {
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

    public static void invokeJob() throws SelfJarException, IOException, InterruptedException {

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
    public static void invokeCharun(String nextJarPath) throws IOException, InterruptedException, SelfJarException {

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
