package test;

import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OneShotTests extends BaseJUnitTest {

    @BeforeClass
    public static void setUpClass() {
        try {

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ExceptionInInitializerError("Setup Error");
        }
    }

    @Test
    @Ignore
    public void tOneShot() throws IOException, InterruptedException {
        String[] args = new String[2];
        args[0] = "[sj]install=../MoreItalians/target/MoreItalians.zip";
        args[1] = "[sj]main=MoreItalians-1.0.0-SNAPSHOT.jar";
        SelfJarInstance.start(args);
    }

    @Test
    @Ignore
    public void tBuildDB500() throws IOException, InterruptedException {
        String[] args = new String[20];
        args[0] = "[sj]install=../MoreItalians/target/MoreItalians.zip";
        args[1] = "[sj]main=MoreItalians-1.0.0-SNAPSHOT.jar";
        args[2] = "[sj]info=true";
        args[3] = "[sj]loglevel=debug";
        args[4] = "[sj]addenv=nItaliansToGenerate=500";
        args[5] = "[sj]addpar=-noDisplay";
        SelfJarInstance.start(args);
    }
}
