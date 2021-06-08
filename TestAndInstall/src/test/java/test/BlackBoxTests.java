package test;

import claudiosoft.selfjar.SelfJarException;
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
        args[0] = "[sj]info=true";
        args[1] = "[sj]loglevel=debug";
        args[2] = "[sj]addenv=nItaliansToGenerate=3";
        args[3] = "[sj]addpar=-noDisplay";
        SelfJarInstance.start(args);
    }

    @Test
    public void t02InstallJob() throws IOException, InterruptedException {
        String[] args = new String[5];
        args[0] = "[sj]info=true";
        args[1] = "[sj]loglevel=debug";
        args[2] = "[sj]install=../MoreItalians/target/MoreItalians.zip";
        args[3] = "[sj]main=MoreItalians-1.0.0-SNAPSHOT.jar";
        SelfJarInstance.start(args);
    }

    @Test
    public void t03InstallClean() throws IOException, InterruptedException {
        String[] args = new String[3];
        args[0] = "[sj]info=true";
        args[1] = "[sj]loglevel=debug";
        args[2] = "[sj]install=clean";
        SelfJarInstance.start(args);
    }

    @Test
    public void t04CleanContext() throws InterruptedException, IOException {
        String[] args = new String[4];
        args[0] = "[sj]info=true";
        args[1] = "[sj]loglevel=debug";
        args[2] = "[sj]delenv=nItaliansToGenerate";
        args[3] = "[sj]delpar=-noDisplay";
        SelfJarInstance.start(args);
    }

    @Test
    public void t05ImportInWS() throws InterruptedException, IOException {
        File testFile = TestResource.extractToFile("test/anagraphic_2000.txt");
        String[] args = new String[4];
        args[0] = "[sj]loglevel=debug";
        args[1] = String.format("[sj]import=%s;pippo/anagraphic.txt;true", testFile.getAbsolutePath());
        SelfJarInstance.start(args);
    }

    @Test
    public void t06ExportWS() throws InterruptedException, IOException, SelfJarException {
        String[] args = new String[4];
        args[0] = "[sj]info=true";
        args[1] = "[sj]loglevel=debug";
        args[2] = "[sj]export=./workspace";
        SelfJarInstance.start(args);
        Assert.assertTrue(new File("./workspace/pippo/anagraphic.txt").exists());
        deleteDirectory(new File("./workspace"));
    }

    @Test
    public void t07DeleteFiles() throws InterruptedException, IOException, SelfJarException {
        String[] args = new String[10];
        args[0] = "[sj]loglevel=debug";
        args[1] = "[sj]delete=anagraphic.txt";
        args[2] = "[sj]delete=pippo/anagraphic.txt";
        args[2] = "[sj]delete=pippo";
        args[3] = "[sj]export=./workspace";
        SelfJarInstance.start(args);
        Assert.assertTrue(!new File("./workspace/anagraphic.txt").exists());
        Assert.assertTrue(!new File("./workspace/pippo/anagraphic.txt").exists());
        deleteDirectory(new File("./workspace"));
    }

    private void deleteDirectory(File directoryToBeDeleted) throws SelfJarException {
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
