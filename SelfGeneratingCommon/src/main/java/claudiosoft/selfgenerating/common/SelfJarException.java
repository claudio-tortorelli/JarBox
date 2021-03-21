package claudiosoft.selfgenerating.common;

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
