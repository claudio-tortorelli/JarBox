package esecurity.selfgeneratingjar;

import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Claudio
 */
public class DaemonThread extends Thread {

    private BasicConsoleLogger logger;

    public DaemonThread(String name, BasicConsoleLogger logger) {
        super(name);
        this.logger = logger;
    }

    public void run() {
        final ArrayList<String> command = new ArrayList<>();
        command.add(Status.getJavaRuntime());
        command.add("-jar");
        command.add(Status.getCurrentJar().getPath());
        if (Status.getParent() == null) {

            /////////// NON MI CONVINCE QUESTO PASSAGGIO DI PARAMETRI
            logger.info("starting myself...");
            command.add("parent=");
            command.add(Status.getCurrentJar().getAbsolutePath());
            command.add("count=");
            command.add(String.format("%d", Status.getRebuildCount() + 1));
            final ProcessBuilder builder = new ProcessBuilder(command);
            try {
                Process p = builder.start();
                Thread.sleep(200);
            } catch (IOException | InterruptedException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }
}
