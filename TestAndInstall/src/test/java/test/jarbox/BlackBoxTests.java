/**
 * JarBoxProject - https://github.com/claudio-tortorelli/JarBox/
 *
 * MIT License - 2021
 */
package test.jarbox;

import claudiosoft.jarbox.Params;
import java.io.File;
import java.io.IOException;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BlackBoxTests extends BaseJUnitTest {

    @Test
    public void t01UpdateContext() throws InterruptedException, IOException {
        String[] args = new String[4];
        args[0] = Params.PARAM_PREFIX + "info=true";
        args[1] = Params.PARAM_PREFIX + "loglevel=debug";
        args[2] = Params.PARAM_PREFIX + "addenv=nItaliansToGenerate=3";
        args[3] = Params.PARAM_PREFIX + "addpar=-noDisplay";
        JarBoxInstance.start(args);
    }

    @Test
    public void t02InstallJob() throws IOException, InterruptedException {
        String[] args = new String[5];
        args[0] = Params.PARAM_PREFIX + "info=true";
        args[1] = Params.PARAM_PREFIX + "loglevel=debug";
        args[2] = Params.PARAM_PREFIX + "install=../MoreItalians/target/MoreItalians.zip";
        args[3] = Params.PARAM_PREFIX + "main=MoreItalians-1.0.0-SNAPSHOT.jar";
        JarBoxInstance.start(args);
    }

    @Test
    public void t03InstallClean() throws IOException, InterruptedException {
        String[] args = new String[3];
        args[0] = Params.PARAM_PREFIX + "info=true";
        args[1] = Params.PARAM_PREFIX + "loglevel=debug";
        args[2] = Params.PARAM_PREFIX + "install=clean";
        JarBoxInstance.start(args);
    }

    @Test
    public void t04CleanContext() throws InterruptedException, IOException {
        String[] args = new String[4];
        args[0] = Params.PARAM_PREFIX + "info=true";
        args[1] = Params.PARAM_PREFIX + "loglevel=debug";
        args[2] = Params.PARAM_PREFIX + "delenv=nItaliansToGenerate";
        args[3] = Params.PARAM_PREFIX + "delpar=-noDisplay";
        JarBoxInstance.start(args);
    }

    @Test
    public void t05ImportInWS() throws InterruptedException, IOException {
        File testFile = TestResource.extractToFile("test/anagraphic_2000.txt");
        String[] args = new String[4];
        args[0] = Params.PARAM_PREFIX + "loglevel=debug";
        args[1] = String.format(Params.PARAM_PREFIX + "import=%s;pippo/anagraphic.txt;true", testFile.getAbsolutePath());
        JarBoxInstance.start(args);
    }

    @Test
    public void t06ExportWS() throws InterruptedException, IOException {
        String[] args = new String[4];
        args[0] = Params.PARAM_PREFIX + "info=true";
        args[1] = Params.PARAM_PREFIX + "loglevel=debug";
        args[2] = Params.PARAM_PREFIX + "export=./workspace";
        JarBoxInstance.start(args);
        boolean exist = new File("./workspace/pippo/anagraphic.txt").exists();
        deleteDirectory(new File("./workspace"));
        Assert.assertTrue(exist);
    }

    @Test
    public void t07DeleteFiles() throws InterruptedException, IOException {
        String[] args = new String[10];
        args[0] = Params.PARAM_PREFIX + "loglevel=debug";
        args[1] = Params.PARAM_PREFIX + "delete=anagraphic.txt";
        args[2] = Params.PARAM_PREFIX + "delete=pippo/anagraphic.txt";
        args[3] = Params.PARAM_PREFIX + "delete=pippo";
        args[4] = Params.PARAM_PREFIX + "export=./workspace";
        JarBoxInstance.start(args);
        boolean exist1 = !new File("./workspace/anagraphic.txt").exists();
        boolean exist2 = !new File("./workspace/pippo/anagraphic.txt").exists();
        deleteDirectory(new File("./workspace"));
        Assert.assertTrue(exist1);
        Assert.assertTrue(exist2);
    }

    @Test
    public void t08ShowHelp() throws InterruptedException, IOException {
        String[] args = new String[10];
        args[0] = Params.PARAM_PREFIX + "help=true";
        JarBoxInstance.start(args);
    }

    private void deleteDirectory(File directoryToBeDeleted) {
        if (!directoryToBeDeleted.exists()) {
            return;
        }
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directoryToBeDeleted.delete();
    }
}
