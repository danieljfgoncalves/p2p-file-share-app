package util;

import static settings.AppSettings.DEFAULT_SEPARATOR;

/**
 * Util class to manipulate strings.
 * <p>
 * Created by 2DD - Group SNOW WHITE {1151452, 1151031, 1141570, 1151088}
 */
public final class StringUtil {

    private StringUtil() {
        //this prevents even the native class from
        //calling this ctor as well :
        throw new AssertionError();
    }

    /**
     * Converts an array of strings to a comma separated string
     *
     * @param array the array to convert
     * @return a comma separated string
     */
    public static String arrayToString(String[] array) {

        StringBuilder tmp = new StringBuilder();
        for (String ext :
                array) {
            tmp.append(ext.concat(DEFAULT_SEPARATOR));
        }
        tmp = new StringBuilder(tmp.substring(0, tmp.length() - 1));

        return tmp.toString();
    }
}
