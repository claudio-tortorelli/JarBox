package esecurity.selfgeneratingjar;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 *
 * @author claudio.tortorelli
 */
public class SelfJar {

    private BasicConsoleLogger logger;

    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        BasicConsoleLogger logger = new BasicConsoleLogger(BasicConsoleLogger.LogLevel.DEBUG, "SelfJar");

        try {
            logger.info("SelfJar started");
            if (args.length == 0) {
                ///////////////////////////////////////////////////////
                args = new String[20];
//            args[0] = "parent=c:\\canc\\metoo.jar";
                ///////////////////////////////////////////////////////
            }
            Utils.parseArgs(args);
            SelfJar selfJar = new SelfJar(logger);
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
            System.exit(Constants.RET_CODE_ERR);
        } finally {

        }
    }

    public SelfJar(BasicConsoleLogger logger) throws URISyntaxException, IOException, InterruptedException {
        this.logger = logger;

        Status.init(this.getClass());
        Status.print(logger);
//        if (Status.getIncarnationCount() < 3) {
//            restartMySelf();
//        }
    }

    private void restartMySelf() throws IOException, InterruptedException {
        DaemonThread t1 = new DaemonThread("666", logger);
        t1.setDaemon(true);
        t1.start();
        t1.join();
        System.exit(0);
    }

}
