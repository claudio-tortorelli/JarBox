/**
 * JarBoxProject - https://github.com/claudio-tortorelli/JarBox/
 *
 * MIT License - 2021
 */
package test.jarbox;

import claudiosoft.jarbox.Params;
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
        args[0] = Params.PARAM_PREFIX + "install=../MoreItalians/target/MoreItalians.zip";
        args[1] = Params.PARAM_PREFIX + "main=MoreItalians-1.0.0-SNAPSHOT.jar";
        JarBoxInstance.start(args);
    }

    @Test
    @Ignore
    public void tBuildDB500() throws IOException, InterruptedException {
        String[] args = new String[20];
        args[0] = Params.PARAM_PREFIX + "install=../MoreItalians/target/MoreItalians.zip";
        args[1] = Params.PARAM_PREFIX + "main=MoreItalians-1.0.0-SNAPSHOT.jar";
        args[2] = Params.PARAM_PREFIX + "info=true";
        args[3] = Params.PARAM_PREFIX + "loglevel=debug";
        args[4] = Params.PARAM_PREFIX + "addenv=nItaliansToGenerate=500";
        args[5] = Params.PARAM_PREFIX + "addpar=-noDisplay";
        JarBoxInstance.start(args);
    }
}
