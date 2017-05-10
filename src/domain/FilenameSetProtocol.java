package domain;

import java.io.File;
import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Represents a application protocol to parse a datagram packet into a FilenameItemSet.
 * <p>
 * Created by danielGoncalves on 10/05/17.
 */
public final class FilenameSetProtocol {

    private static final int PAYLOAD_SIZE = 512;

    private static final int TCP_PORT_INDEX = 0;
    private static final int USERNAME_INDEX = 1;
    private static final int COUNT_INDEX = 52;
    private static final int FIRST_ELEMENT_INDEX = 53;

    /**
     * The caller references the static methods using <tt>FilenameSetProtocol.EMPTY_STRING</tt>,
     * and so on. Thus, the caller should be prevented from constructing objects of
     * this class, by declaring this private constructor.
     */
    private FilenameSetProtocol() {
        //this prevents even the native class from
        //calling this ctor as well :
        throw new AssertionError();
    }

    public static void parsePacket(DatagramPacket packet, FilenameItemSet set) {

        byte[] bytes = packet.getData();

        InetAddress hostAddress = packet.getAddress();
        Integer tcpPort = (new Byte(bytes[TCP_PORT_INDEX])).intValue();
        String username = new String(bytes, USERNAME_INDEX, (COUNT_INDEX - USERNAME_INDEX));
        int itemCount = (new Byte(bytes[COUNT_INDEX])).intValue();

        int filenameSize = (new Byte(bytes[FIRST_ELEMENT_INDEX])).intValue();
        int nextIndex = FIRST_ELEMENT_INDEX + 1;
        for (int i = 0; i < itemCount; i++) {

            String filename = new String(bytes, nextIndex, filenameSize);

            FilenameItem item = new FilenameItem(filename, username, hostAddress, tcpPort);

            set.add(item); // add to set

            // Set next filenameSize && nextItem
            filenameSize = (new Byte(bytes[nextIndex + filenameSize])).intValue();
            nextIndex = nextIndex + filenameSize + 1;
        }

        set.notifyChanges(); // Notify observers of changes to filenameSet
    }

    public static byte[] parseDirectory(File[] files) {

        // TODO : Implement send protocol
        throw new UnsupportedOperationException("Not Implemented yet.");

    }
}
