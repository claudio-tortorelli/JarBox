package claudiosoft.selfgeneratingjar;

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

    }
}
