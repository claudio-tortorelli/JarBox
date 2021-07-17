/**
 * JarBoxProject - https://github.com/claudio-tortorelli/JarBox/
 *
 * MIT License - 2021
 */
package test.jarbox;

import claudiosoft.jarbox.Params;
import java.io.File;
import java.io.IOException;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StressTests extends BaseJUnitTest {

    @Test
    public void t01Prepare() throws InterruptedException, IOException {
        String[] args = new String[4];
        args[0] = Params.PARAM_PREFIX + "info=true";
        args[1] = Params.PARAM_PREFIX + "install=../MoreItalians/target/MoreItalians.zip";
        args[2] = Params.PARAM_PREFIX + "main=MoreItalians-1.0.0-SNAPSHOT.jar";
        args[3] = Params.PARAM_PREFIX + "addpar=-noDisplay";
        JarBoxInstance.start(args);
    }

    @Test
    public void t02Execute() throws IOException, InterruptedException {

        long start = System.currentTimeMillis();
        String[] args = new String[15];
        args[0] = Params.PARAM_PREFIX + "loglevel=debug";
        JarBoxInstance.start(args);
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        long firstElapsed = timeElapsed;
        System.out.println(String.format("first execution (%.1f mb), time %.2f sec",
                JarBox.size(), timeElapsed / 1000.0));

        File testFile = TestResource.extractToFile("test/anagraphic_2000.txt");

        start = System.currentTimeMillis();
        args[0] = Params.PARAM_PREFIX + "loglevel=debug";
        args[1] = String.format(Params.PARAM_PREFIX + "import=%s;anagraphic_2000.txt;true", testFile.getAbsolutePath());
        JarBoxInstance.start(args);
        finish = System.currentTimeMillis();
        long prevElapsed = timeElapsed;
        timeElapsed = finish - start;
        System.out.println(String.format("---> second execution (%.1f mb), time %.2f sec, delta %.2f sec, delta base %.2f sec",
                JarBox.size(), timeElapsed / 1000.0, (timeElapsed - prevElapsed) / 1000.0, (timeElapsed - firstElapsed) / 1000.0));

        File testFile2 = TestResource.extractToFile("test/anagraphic_100000.txt");

        start = System.currentTimeMillis();
        args[0] = Params.PARAM_PREFIX + "loglevel=debug";
        args[1] = String.format(Params.PARAM_PREFIX + "import=%s;anagraphic_100000.txt;true", testFile2.getAbsolutePath());
        JarBoxInstance.start(args);
        finish = System.currentTimeMillis();
        prevElapsed = timeElapsed;
        timeElapsed = finish - start;
        System.out.println(String.format("---> third execution (%.1f mb), time %.2f sec, delta %.2f sec, delta base %.2f sec",
                JarBox.size(), timeElapsed / 1000.0, (timeElapsed - prevElapsed) / 1000.0, (timeElapsed - firstElapsed) / 1000.0));

        start = System.currentTimeMillis();
        args[0] = Params.PARAM_PREFIX + "loglevel=debug";
        args[1] = String.format(Params.PARAM_PREFIX + "import=%s;anagraphic_100000_2.txt;true", testFile2.getAbsolutePath());
        JarBoxInstance.start(args);
        finish = System.currentTimeMillis();
        prevElapsed = timeElapsed;
        timeElapsed = finish - start;
        System.out.println(String.format("---> fourth execution (%.1f mb), time %.2f sec, delta %.2f sec, delta base %.2f sec",
                JarBox.size(), timeElapsed / 1000.0, (timeElapsed - prevElapsed) / 1000.0, (timeElapsed - firstElapsed) / 1000.0));

        start = System.currentTimeMillis();
        args[0] = Params.PARAM_PREFIX + "loglevel=debug";
        args[1] = String.format(Params.PARAM_PREFIX + "import=%s;anagraphic_100000_3.txt;true", testFile2.getAbsolutePath());
        args[2] = String.format(Params.PARAM_PREFIX + "import=%s;anagraphic_100000_4.txt;true", testFile2.getAbsolutePath());
        args[3] = String.format(Params.PARAM_PREFIX + "import=%s;anagraphic_100000_5.txt;true", testFile2.getAbsolutePath());
        args[4] = String.format(Params.PARAM_PREFIX + "import=%s;anagraphic_100000_6.txt;true", testFile2.getAbsolutePath());
        args[5] = String.format(Params.PARAM_PREFIX + "import=%s;anagraphic_100000_7.txt;true", testFile2.getAbsolutePath());
        args[6] = String.format(Params.PARAM_PREFIX + "import=%s;anagraphic_100000_8.txt;true", testFile2.getAbsolutePath());
        args[7] = String.format(Params.PARAM_PREFIX + "import=%s;anagraphic_100000_9.txt;true", testFile2.getAbsolutePath());
        args[8] = String.format(Params.PARAM_PREFIX + "import=%s;anagraphic_100000_10.txt;true", testFile2.getAbsolutePath());
        JarBoxInstance.start(args);
        finish = System.currentTimeMillis();
        prevElapsed = timeElapsed;
        timeElapsed = finish - start;
        System.out.println(String.format("---> fifth execution (%.1f mb), time %.2f sec, delta %.2f sec, delta base %.2f sec",
                JarBox.size(), timeElapsed / 1000.0, (timeElapsed - prevElapsed) / 1000.0, (timeElapsed - firstElapsed) / 1000.0));
    }

    @Test
    public void t03Clean() throws InterruptedException, IOException {
        String[] args = new String[15];
        args[1] = Params.PARAM_PREFIX + "install=clean";
        args[2] = Params.PARAM_PREFIX + "delpar=-noDisplay";
        args[3] = Params.PARAM_PREFIX + "delete=anagraphic.txt";
        args[4] = Params.PARAM_PREFIX + "delete=anagraphic_2000.txt";
        args[5] = Params.PARAM_PREFIX + "delete=anagraphic_100000.txt";
        args[6] = Params.PARAM_PREFIX + "delete=anagraphic_100000_2.txt";
        args[7] = Params.PARAM_PREFIX + "delete=anagraphic_100000_3.txt";
        args[8] = Params.PARAM_PREFIX + "delete=anagraphic_100000_4.txt";
        args[9] = Params.PARAM_PREFIX + "delete=anagraphic_100000_5.txt";
        args[10] = Params.PARAM_PREFIX + "delete=anagraphic_100000_6.txt";
        args[11] = Params.PARAM_PREFIX + "delete=anagraphic_100000_7.txt";
        args[12] = Params.PARAM_PREFIX + "delete=anagraphic_100000_8.txt";
        args[13] = Params.PARAM_PREFIX + "delete=anagraphic_100000_9.txt";
        args[14] = Params.PARAM_PREFIX + "delete=anagraphic_100000_10.txt";
        JarBoxInstance.start(args);
    }
}
