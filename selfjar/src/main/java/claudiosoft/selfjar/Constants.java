package claudiosoft.selfjar;

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

    public static final String TMP_SELFJAR_FOLDER = "selfJar";
    public static final String JOB_FOLDER = "job";
    public static final String CONTEXT_FOLDER = "context";
    public static final String JOB_ENTRY = "job/job.zip";
    public static final String WS_ENTRY_FOLDER = "job/workspace";

    public static final String LOGGER_NAME = "SelfJar";

}
