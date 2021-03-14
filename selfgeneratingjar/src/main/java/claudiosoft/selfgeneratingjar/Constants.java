package claudiosoft.selfgeneratingjar;

import java.util.TimeZone;

/**
 *
 * @author Claudio
 */
public class Constants {

    public static final String DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";
    public static final String DATE_FORMAT_SHORT = "yyyyMMddHHmmss";
    public static final TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone("UTC");

    public static final int RET_CODE_OK = 0;
    public static final int RET_CODE_ERR = 1;

    public static final int BUFFER_SIZE = 4096;

    public static final String INTERNAL_STATUS_NAME = "status.txt";
    public static final String TMP_SELFJAR_FOLDER = "selfJar";

    public static final String CONTEXT_FILENAME = "context.txt";
    public static final String CTX_COMMENT = "#";
    public static final String CTX_COUNT = "EXECOUNT";

}
