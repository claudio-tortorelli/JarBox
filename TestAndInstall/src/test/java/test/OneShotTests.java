package test;

import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BlackBoxTests extends BaseJUnitTest {

    @BeforeClass
    public static void setUpClass() {
        try {

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ExceptionInInitializerError("Setup Error");
        }
    }

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
    public void t100Final() throws IOException, InterruptedException {
        String[] args = new String[2];
        args[0] = "[sj]install=../MoreItalians/target/MoreItalians.zip";
        args[1] = "[sj]main=MoreItalians-1.0.0-SNAPSHOT.jar";
        SelfJarInstance.start(args);
    }

}
