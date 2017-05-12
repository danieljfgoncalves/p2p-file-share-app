package util;

/**
 * Represents the common constants of the application.
 *
 * Created by danielGoncalves on 09/05/17.
 */
public final class Constants {

    /**** GLOBAL CONSTANTS ****/
    public static final int REFRESH_FILENAME_TIMELIMIT = 25; // Refresh time for a filename in seconds
    public static final int PAYLOAD_SIZE = 512; // DatagramPacket data size limit

    public static final int MAX_TCP_CONNECTIONS = 10; // Maximum number of tcp connections at the same time (# of threads).


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
