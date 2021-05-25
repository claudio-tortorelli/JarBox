package test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.Scanner;

/**
 *
 * @author claudio.tortorelli
 */
public class SelfJarInstance {

    public static void start(String[] args) throws IOException, InterruptedException {
        File sjFile = SelfJar.get();
        LinkedList<String> pbArgs = new LinkedList<>();
        pbArgs.add("java");
        pbArgs.add("-jar");
        pbArgs.add(sjFile.getAbsolutePath());
        for (String arg : args) {
            if (arg != null && !arg.isEmpty()) {
                pbArgs.add(arg);
            }
        }

        ProcessBuilder processBuilder = new ProcessBuilder(pbArgs);
        Process insideProc = processBuilder.start();
        inheritIO(insideProc.getInputStream(), System.out);
        inheritIO(insideProc.getErrorStream(), System.err);

        insideProc.waitFor();
    }

    public static void inheritIO(final InputStream src, final PrintStream dest) {
        new Thread(new Runnable() {
            public void run() {
                Scanner sc = new Scanner(src);
                while (sc.hasNextLine()) {
                    dest.println(sc.nextLine());
                }
            }
        }).start();
    }
}
