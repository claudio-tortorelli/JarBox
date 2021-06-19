package claudiosoft.jarbox;

/**
 *
 * @author Claudio
 */
public class JarBoxException extends Exception {

    public JarBoxException(String errorMessage) {
        super(errorMessage);
    }

    public JarBoxException(String errorMessage, Throwable ex) {
        super(errorMessage, ex);
    }
}
