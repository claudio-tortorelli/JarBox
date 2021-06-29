/**
 * JarBoxProject - https://github.com/claudio-tortorelli/JarBox/
 *
 * MIT License - 2021
 */
package test.jarbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;

/**
 *
 * @author claudio.tortorelli
 */
public class TestResource {

    /**
     * return the stream to the resource stored inside the common package
     *
     * @param resourcePath
     * @return
     * @throws IOException
     */
    public static InputStream getStream(String resourcePath) throws IOException {

        URL resourceURL = TestResource.class.getClassLoader().getResource(resourcePath);
        String resPath = resourceURL.toString();
        if (resPath.startsWith("jar:")) {
            JarURLConnection connection = (JarURLConnection) resourceURL.openConnection();
            return connection.getInputStream();
        } else if (resPath.startsWith("file:")) {
            FileInputStream is = new FileInputStream(new File(resourceURL.getFile()));
            return is;
        }
        return null;
    }

    /**
     * extract the resource to the specified path
     *
     * @param resourcePath
     * @return
     * @throws IOException
     */
    public static File extractToFile(String resourcePath) throws IOException {
        return TestResource.extractToFile(resourcePath, null, null);
    }

    public static File extractToFile(String resourcePath, String fileName) throws IOException {
        return TestResource.extractToFile(resourcePath, fileName, null);
    }

    public static File extractToFile(String resourcePath, String fileName, String destFolder) throws IOException {

        URL resourceURL = TestResource.class.getClassLoader().getResource(resourcePath);

        InputStream is = null;
        FileOutputStream fos = null;
        File retFile = null;

        try {
            String resPath = resourceURL.toString();
            if (resPath.startsWith("jar:")) {
                JarURLConnection connection = (JarURLConnection) resourceURL.openConnection();
                is = connection.getInputStream();
            } else if (resPath.startsWith("file:")) {
                is = new FileInputStream(new File(resourceURL.getFile()));
            } else {
                return null;
            }

            if (destFolder == null || destFolder.isEmpty()) {
                destFolder = BaseJUnitTest.getOutFolder();
            }
            if (fileName == null || fileName.isEmpty()) {
                int slashIndex = resourcePath.lastIndexOf("/");
                if (slashIndex < 0) {
                    fileName = resourcePath;
                } else {
                    fileName = resourcePath.substring(slashIndex + 1);
                }
            }
            String destPath = String.format("%s%s%s", destFolder, File.separator, fileName);

            retFile = new File(destPath);
            if (!retFile.exists()) {
                retFile.createNewFile();
            }

            fos = new FileOutputStream(retFile);
            byte[] buffer = new byte[4096];
            int byteRead = is.read(buffer);
            while (byteRead > 0) {
                fos.write(buffer, 0, byteRead);
                byteRead = is.read(buffer);
            }

        } finally {
            if (is != null) {
                is.close();
            }
            if (fos != null) {
                fos.close();
            }
        }

        return retFile;
    }

    /**
     *
     * @param fileName
     * @return
     * @throws IOException
     */
    public static File toOutputFile(String fileName) throws IOException {
        return TestResource.toOutputFile(fileName, null);
    }

    public static File toOutputFile(String fileName, String destFolder) throws IOException {
        if (destFolder == null || destFolder.isEmpty()) {
            destFolder = BaseJUnitTest.getOutFolder();
        }
        if (fileName == null || fileName.isEmpty()) {
            throw new IOException("Output file name not defined!");
        }
        return new File(String.format("%s%s%s", destFolder, File.separator, fileName));
    }

}
