package esecurity.selfgeneratingjar;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

/**
 *
 * @author Claudio
 */
public class Utils {

    private static RandomAccessFile lockFis = null;
    private static FileLock fileLock;

    public static File getFileFromRes(String resPath) throws IOException {

        URL resUrl = Thread.currentThread().getContextClassLoader().getResource(resPath);
        if (resUrl == null) {
            throw new FileNotFoundException("not found " + resPath);
        }
        String urlStr = resUrl.toString();
        InputStream is = null;
        File tmp = null;
        try {
            if (urlStr.startsWith("jar:")) {
                final JarURLConnection connection = (JarURLConnection) resUrl.openConnection();
                is = connection.getInputStream();
                tmp = File.createTempFile("native", ".tmp");
                java.nio.file.Files.copy(is, tmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return tmp;
            } else if (urlStr.startsWith("file:")) {
                return new File(resUrl.getFile());
            }
        } finally {
            if (is != null) {
                is.close();
            }
            if (tmp != null) {
                tmp.deleteOnExit();
            }
        }
        throw new FileNotFoundException("not found " + resPath);
    }

    public static String dateToString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

    public static Date stringToDate(String date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.parse(date);
    }

    public static void parseArgs(String[] args) {
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
                Status.setParent(new File(value));
            } else if (param.startsWith("count")) {
                int counter = Integer.parseInt(value);
                Status.setRebuildCount(counter);
            } else {
                throw new IllegalArgumentException("unrecognized input argument: " + param);
            }
        }
    }

    public static boolean testLockFile(File testFile) {
        boolean bLocked = false;
        try (RandomAccessFile fis = new RandomAccessFile(testFile, "rw")) {
            FileLock lck = fis.getChannel().lock();
            lck.release();
        } catch (Exception ex) {
            bLocked = true;
        }
        if (bLocked) {
            return bLocked;
        }
        // try further with rename
        String parent = testFile.getParent();
        String rnd = UUID.randomUUID().toString();
        File newName = new File(parent + "/" + rnd);
        if (testFile.renameTo(newName)) {
            newName.renameTo(testFile);
        } else {
            bLocked = true;
        }
        return bLocked;
    }

    public static void doLock(File lockFile) throws FileNotFoundException, IOException {
        lockFis = new RandomAccessFile(lockFile, "rw");
        fileLock = lockFis.getChannel().lock();
    }

    public static void doUnlock() throws IOException {
        fileLock.release();
        lockFis.close();
    }

    /**
     * Null safe close of the given {@link Closeable} suppressing any exception.
     *
     * @param closeable to be closed
     */
    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    public static byte[] getSHA256(String text) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
        return hash;
    }

    public static byte[] getSHA256(InputStream inputStream) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] dataBuffer = new byte[Constants.BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = inputStream.read(dataBuffer)) >= 0) {
            digest.update(dataBuffer, 0, bytesRead);
        }
        byte[] hash = digest.digest();
        return hash;
    }
}
