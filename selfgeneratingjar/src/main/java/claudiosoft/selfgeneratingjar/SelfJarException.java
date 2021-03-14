package claudiosoft.selfgeneratingjar;

/**
 *
 * @author Claudio
 */
public class SelfJarException extends Exception {

    public SelfJarException(String errorMessage) {
        super(errorMessage);
    }

    public SelfJarException(String errorMessage, Throwable ex) {
        super(errorMessage, ex);
    }
}
