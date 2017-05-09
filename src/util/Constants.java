package util;

/**
 * Created by danielGoncalves on 09/05/17.
 */
public final class Constants {

    /**** CONSTANTS ****/
    public static final int REFRESH_FILENAME_TIMELIMIT = 45; // Refresh time for a filename in seconds


    /**
     * The caller references the constants using <tt>Consts.EMPTY_STRING</tt>,
     * and so on. Thus, the caller should be prevented from constructing objects of
     * this class, by declaring this private constructor.
     */
    private Constants() {
        //this prevents even the native class from
        //calling this ctor as well :
        throw new AssertionError();
    }
}
