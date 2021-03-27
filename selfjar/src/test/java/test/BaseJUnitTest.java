package test;

import java.io.File;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.rules.TestName;

/**
 * Base class for all esecurity tests:
 *
 * It verifies that FixMethodOrder annotation is defined into derived test. The
 * test fail otherwise
 *
 * Optionally it creates the TEST_OUT_FOLDER and clean its content (default is
 * true)
 *
 * It setups the test's base truststore needed to accomplish network connections
 * over SSL
 *
 * @author marco.pancioni
 * @author claudio.tortorelli
 */
public class BaseJUnitTest {

    // you can define a different buildDirectory different than standard "./target" using following maven plugin into project pom
    // example:
    //    <plugin>
    //        <groupId>org.apache.maven.plugins</groupId>
    //        <artifactId>maven-surefire-plugin</artifactId>
    //        <version>2.21.0</version>
    //        <configuration>
    //            <systemPropertyVariables>
    //                <buildDirectory>${project.build.directory}</buildDirectory>
    //            </systemPropertyVariables>
    //        </configuration>
    //    </plugin>
    private static final String outFolder = String.format("%s/test-output", System.getProperty("buildDirectory", "./target"));
    private static boolean cleanFolder = true;
    private static long startTime = 0;

    @Rule
    public TestName testName = new TestName();

    public BaseJUnitTest() {
        this(true);
    }

    public BaseJUnitTest(boolean cleanFolder) {
        this(cleanFolder, true);
    }

    public BaseJUnitTest(boolean cleanFolder, boolean checkTestName) {
        // richiede che la classe di test abbia l'annotazione FixMethodOrder
        Assert.assertNotNull("missing FixMethodOrder annotation", this.getClass().getAnnotation(FixMethodOrder.class));
        if (checkTestName) {
            // richiede che la classe di test abbia la parola 'Test' nel nome, in modo da poter essere eseguita in automatico
            // non utile per le classi da includere nelle test suite
            Assert.assertTrue("missing 'Test' in class name", this.getClass().getName().contains("Test")||this.getClass().getName().endsWith("IT"));
        }
        this.cleanFolder = cleanFolder;
    }

    @BeforeClass
    public static void setUpClass() {
        // build the test output folder and remove all files inside
        File testFolder = new File(outFolder);
        testFolder.mkdirs();
        if (cleanFolder) {
            File[] files = testFolder.listFiles();
            if (files != null && files.length > 0) { // remove all previous outputs
                for (File f : files) {
                    if (!f.isDirectory()) {
                        f.delete();
                    }
                }
            }
        }

        // needed to have truststore available into tests
        File sdkTrustStore = null;
        try {
            // cacerts for TSL ssl connection and Aruba TSA
            sdkTrustStore = TestResource.extractToFile("base/truststore/cacerts_test");
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        Assert.assertTrue("truststore not found", sdkTrustStore != null && sdkTrustStore.exists());
        System.setProperty("javax.net.ssl.trustStore", sdkTrustStore.getAbsolutePath());
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        System.out.println(String.format("[TEST START] - %s", testName.getMethodName()));
        startTime = System.currentTimeMillis();
    }

    @After
    public void tearDown() {
        double elapsedTime = (double) (System.currentTimeMillis() - startTime) / 1000;
        System.out.println(String.format("[TEST END] - %s - done in %.3f sec", testName.getMethodName(), elapsedTime));
    }

    /**
     * Test output folder
     *
     * @return
     */
    public static String getOutFolder() {
        return outFolder;
    }

}
