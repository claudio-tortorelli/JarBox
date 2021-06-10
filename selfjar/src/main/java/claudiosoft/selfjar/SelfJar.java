package claudiosoft.selfjar;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author claudio.tortorelli
 */
public final class SelfJar {

    private static final File FILE_LOCK = new File(System.getProperty("user.home") + File.separator + ".selfgenerating_lock");

    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        try {
            // avoid multiple instances
            Utils.testLockFile(FILE_LOCK);
            Utils.doLock(FILE_LOCK);
            new SelfJar(args);
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
            System.exit(Constants.RET_CODE_ERR);
        } finally {
            Utils.doUnlock();
        }
    }

    public SelfJar(String[] args) throws URISyntaxException, IOException, InterruptedException, NoSuchAlgorithmException, SelfJarException {

        try {
            // initialization
            Params.get().parseArgs(args);
            BasicConsoleLogger.get().info("SelfJar started");

            BasicConsoleLogger.get().debug("jar expanding...");
            IO.get().out();

            BasicConsoleLogger.get().debug("apply parameters...");
            IO.get().applyParams();

            if (Params.get().info()) {
                BasicConsoleLogger.get().info(toString());
            }
            IO.invokeJob();

            // create updated jar
            BasicConsoleLogger.get().debug("creating next jar...");
            File nextJar = IO.get().in();

            // invoke charun to bring nextJar as current
            IO.invokeCharun(nextJar.getAbsolutePath());
        } finally {
            // unlock any open file
            IO.get().closeAll();
            BasicConsoleLogger.get().debug("closing");
        }
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        String ret = "\n";
        try {
            ret += Identity.get().toString();
            ret += IO.get().toString();
        } catch (SelfJarException ex) {

        }
        return ret;
    }

}
