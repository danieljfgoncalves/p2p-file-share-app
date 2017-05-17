package util;

import static settings.AppSettings.DEFAULT_SEPARATOR;

/**
 * Util class to manipulate strings.
 * <p>
 * Created by danielGoncalves on 11/05/17.
 */
public final class StringUtil {

    private StringUtil() {
        //this prevents even the native class from
        //calling this ctor as well :
        throw new AssertionError();
    }

    public static String arrayToString(String[] array) {

        String tmp = "";
        for (String ext :
                array) {
            tmp += ext.concat(DEFAULT_SEPARATOR);
        }
        tmp = tmp.substring(0, tmp.length() - 1);

        return tmp;
    }
}
