package claudiosoft.selfjar;

import java.util.TimeZone;

/**
 *
 * @author Claudio
 */
public class SelfConstants {

    public static final String DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";
    public static final String DATE_FORMAT_SHORT = "yyyyMMddHHmmss";
    public static final TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone("UTC");

    public static final int RET_CODE_OK = 0;
    public static final int RET_CODE_ERR = 1;

    public static final int BUFFER_SIZE = 4096;

    public static final String INTERNAL_STATUS_NAME = "status.txt";
    public static final String TMP_SELFJAR_FOLDER = "selfJar";

    public static final String PARAM_PREFIX = "[sj]";

    public static final String LOGGER_NAME = "SelfJar";

    public static final String CONTEXT_FULLNAME = "context/context.txt";
    public static final String CTX_COMMENT = "#";
    public static final String CTX_COUNT = "EXECOUNT";
    public static final String CTX_INSTALLEDJOB = "JOB";
    public static final String CTX_JOBPARAM = "JOBPAR";
    public static final String CTX_ENVPARAM = "ENVPAR";
    public static final String CTX_MAIN = "MAIN";
}
