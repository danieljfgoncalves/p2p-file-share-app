package util;

/**
 * Represents the common constants of the application.
 *
 * Created by danielGoncalves on 09/05/17.
 */
public final class Constants {

    /**** GLOBAL CONSTANTS ****/
    public static final int PAYLOAD_SIZE = 512; // DatagramPacket data size limit
    public static final String BROADCAST_STRING = "255.255.255.255"; // Broadcast address
    public static final int SEND_LIST_DELAY = 1; // Send list Timer schedule delay
    public static final String WARNING_PANE_TITLE = "SYSTEM FAILURE";
    public static final String CONFIG_FILENAME = "config.properties";

    /**** EXIT STATUS ****/
    public static final int EXIT_SUCCESS = 0;
    public static final int SOCKET_FAILED = -1;

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
