package dk.magenta.eark.cmis.bridge;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

/**
 * @author DarkStar1.
 */
final public class Utils {

    private Utils() {
    }

    private final Logger logger = LoggerFactory.getLogger(Utils.class);
    /**
     * Convert the gregorian calendar date to ISO8601 date format. (Javascript should find it easier to work with this
     * format)
     * @param nasty_date
     * @return
     */
    public static String convertToISO8601Date(GregorianCalendar nasty_date){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        try {
            return df.format(nasty_date.getTime());
        }
        catch (Exception ge){
            ge.printStackTrace();
        }
        return null;
    }

    /**
     * Given a cmis property of the cmis:propertyName, this will return the string value (propertyName) of that value.
     * @param property
     * @return
     */
    public static String getPropertyPostFixValue(String property){
        return StringUtils.substringAfter(property, ":");
    }

    /**
     * Check (or optionally create) a directory [exists].
     * @param path the path to the directory
     * @param createDir boolean that determines whether or not to create the dir if it doesn't exist
     */
    public static boolean checkDirExists(String path, boolean createDir){
        File dir = new File(path);
        try {
            if(createDir) {
                if (!dir.exists())
                    if (!dir.mkdirs())
                        throw new RuntimeException("Unable to create directory");
                return true;
            }
            else return dir.exists();
        }
        catch (Exception ge){
            ge.printStackTrace();
        }

        return false;
    }
}
