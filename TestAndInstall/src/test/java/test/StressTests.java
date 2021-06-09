package test;

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
        args[0] = "[sj]info=true";
        args[1] = "[sj]install=../MoreItalians/target/MoreItalians.zip";
        args[2] = "[sj]main=MoreItalians-1.0.0-SNAPSHOT.jar";
        args[3] = "[sj]addpar=-noDisplay";
        SelfJarInstance.start(args);
    }

    @Test
    public void t02Execute() throws IOException, InterruptedException {

        long start = System.currentTimeMillis();
        String[] args = new String[15];
        args[0] = "[sj]loglevel=debug";
        SelfJarInstance.start(args);
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        long firstElapsed = timeElapsed;
        System.out.println(String.format("first execution (%.1f mb), time %.2f sec",
                SelfJar.size(), timeElapsed / 1000.0));

        File testFile = TestResource.extractToFile("test/anagraphic_2000.txt");

        start = System.currentTimeMillis();
        args[0] = "[sj]loglevel=debug";
        args[1] = String.format("[sj]import=%s;anagraphic_2000.txt;true", testFile.getAbsolutePath());
        SelfJarInstance.start(args);
        finish = System.currentTimeMillis();
        long prevElapsed = timeElapsed;
        timeElapsed = finish - start;
        System.out.println(String.format("---> second execution (%.1f mb), time %.2f sec, delta %.2f sec, delta base %.2f sec",
                SelfJar.size(), timeElapsed / 1000.0, (timeElapsed - prevElapsed) / 1000.0, (timeElapsed - firstElapsed) / 1000.0));

        File testFile2 = TestResource.extractToFile("test/anagraphic_100000.txt");

        start = System.currentTimeMillis();
        args[0] = "[sj]loglevel=debug";
        args[1] = String.format("[sj]import=%s;anagraphic_100000.txt;true", testFile2.getAbsolutePath());
        SelfJarInstance.start(args);
        finish = System.currentTimeMillis();
        prevElapsed = timeElapsed;
        timeElapsed = finish - start;
        System.out.println(String.format("---> third execution (%.1f mb), time %.2f sec, delta %.2f sec, delta base %.2f sec",
                SelfJar.size(), timeElapsed / 1000.0, (timeElapsed - prevElapsed) / 1000.0, (timeElapsed - firstElapsed) / 1000.0));

        start = System.currentTimeMillis();
        args[0] = "[sj]loglevel=debug";
        args[1] = String.format("[sj]import=%s;anagraphic_100000_2.txt;true", testFile2.getAbsolutePath());
        SelfJarInstance.start(args);
        finish = System.currentTimeMillis();
        prevElapsed = timeElapsed;
        timeElapsed = finish - start;
        System.out.println(String.format("---> fourth execution (%.1f mb), time %.2f sec, delta %.2f sec, delta base %.2f sec",
                SelfJar.size(), timeElapsed / 1000.0, (timeElapsed - prevElapsed) / 1000.0, (timeElapsed - firstElapsed) / 1000.0));

        start = System.currentTimeMillis();
        args[0] = "[sj]loglevel=debug";
        args[1] = String.format("[sj]import=%s;anagraphic_100000_3.txt;true", testFile2.getAbsolutePath());
        args[2] = String.format("[sj]import=%s;anagraphic_100000_4.txt;true", testFile2.getAbsolutePath());
        args[3] = String.format("[sj]import=%s;anagraphic_100000_5.txt;true", testFile2.getAbsolutePath());
        args[4] = String.format("[sj]import=%s;anagraphic_100000_6.txt;true", testFile2.getAbsolutePath());
        args[5] = String.format("[sj]import=%s;anagraphic_100000_7.txt;true", testFile2.getAbsolutePath());
        args[6] = String.format("[sj]import=%s;anagraphic_100000_8.txt;true", testFile2.getAbsolutePath());
        args[7] = String.format("[sj]import=%s;anagraphic_100000_9.txt;true", testFile2.getAbsolutePath());
        args[8] = String.format("[sj]import=%s;anagraphic_100000_10.txt;true", testFile2.getAbsolutePath());
        SelfJarInstance.start(args);
        finish = System.currentTimeMillis();
        prevElapsed = timeElapsed;
        timeElapsed = finish - start;
        System.out.println(String.format("---> fifth execution (%.1f mb), time %.2f sec, delta %.2f sec, delta base %.2f sec",
                SelfJar.size(), timeElapsed / 1000.0, (timeElapsed - prevElapsed) / 1000.0, (timeElapsed - firstElapsed) / 1000.0));
    }

    @Test
    public void t03Clean() throws InterruptedException, IOException {
        String[] args = new String[15];
        args[1] = "[sj]install=clean";
        args[2] = "[sj]delpar=-noDisplay";
        args[3] = "[sj]delete=anagraphic.txt";
        args[4] = "[sj]delete=anagraphic_2000.txt";
        args[5] = "[sj]delete=anagraphic_100000.txt";
        args[6] = "[sj]delete=anagraphic_100000_2.txt";
        args[7] = "[sj]delete=anagraphic_100000_3.txt";
        args[8] = "[sj]delete=anagraphic_100000_4.txt";
        args[9] = "[sj]delete=anagraphic_100000_5.txt";
        args[10] = "[sj]delete=anagraphic_100000_6.txt";
        args[11] = "[sj]delete=anagraphic_100000_7.txt";
        args[12] = "[sj]delete=anagraphic_100000_8.txt";
        args[13] = "[sj]delete=anagraphic_100000_9.txt";
        args[14] = "[sj]delete=anagraphic_100000_10.txt";
        SelfJarInstance.start(args);
    }
}
