package networking;

import domain.RemoteFilename;
import settings.Application;
import util.ByteUtil;
import util.Constants;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a application protocol to parse a datagram packet into a RemoteFilenameList, and vice-versa.
 * <p>
 * Created by 2DD - Group SNOW WHITE {1151452, 1151031, 1141570, 1151088}
 */
public final class RemoteFilenameListProtocol {

    private static final int PROTOCOL_ID = 1;
    private static final int PROTOCOL_VERSION = 1;

    private static final int ID_INDEX = 0;
    private static final int VERSION_INDEX = 1;
    private static final int TCP_PORT_INDEX = 2;
    private static final int FILE_COUNT_INDEX = 6;
    private static final int USERNAME_LENGTH_INDEX = 7;
    private static final int USERNAME_INDEX = 8;

    private static final int ID_SIZE = VERSION_INDEX - ID_INDEX;
    private static final int VERSION_SIZE = TCP_PORT_INDEX - VERSION_INDEX;
    private static final int TCP_PORT_SIZE = FILE_COUNT_INDEX - TCP_PORT_INDEX;
    private static final int FILE_COUNT_SIZE = USERNAME_LENGTH_INDEX - FILE_COUNT_INDEX;

    /**
     * The caller references the static methods using <tt>RemoteFilenameListProtocol.EMPTY_STRING</tt>,
     * and so on. Thus, the caller should be prevented from constructing objects of
     * this class, by declaring this private constructor.
     */
    private RemoteFilenameListProtocol() {
        //this prevents even the native class from
        //calling this constructor as well :
        throw new AssertionError();
    }

    /**
     * Parses a byte array and returns a list of filename items according to application protocol.
     *
     * @param bytes       the data with the items
     * @param hostAddress the address that sent the data
     *
     * @return a list of filename items
     */
    public static List<RemoteFilename> parsePacket(byte[] bytes, InetAddress hostAddress)
            throws IllegalStateException {

        List<RemoteFilename> filenames = new ArrayList<>();

        if (bytes[ID_INDEX] != PROTOCOL_ID || bytes[VERSION_INDEX] != PROTOCOL_VERSION) {
            throw new IllegalStateException("Packet does not abide by this protocol.");
        }

        // If packet is valid
        Integer tcpPort = ByteUtil.bytesToInt(Arrays.copyOfRange(bytes, TCP_PORT_INDEX, (TCP_PORT_INDEX + TCP_PORT_SIZE)));
        int itemCount = ByteUtil.byteToInt(bytes[FILE_COUNT_INDEX]);

        int usernameSize = ByteUtil.byteToInt(bytes[USERNAME_LENGTH_INDEX]);
        String username = new String(bytes, USERNAME_INDEX, usernameSize);

        int nextIndex = ID_SIZE + VERSION_SIZE + TCP_PORT_SIZE + FILE_COUNT_SIZE + 1 /* username length byte */ + usernameSize;
        int filenameSize = ByteUtil.byteToInt(bytes[nextIndex]);
        nextIndex++;

        for (int i = 0; i < itemCount; i++) {

            String filename = new String(bytes, nextIndex, filenameSize);

            RemoteFilename item = new RemoteFilename(filename, username, hostAddress, tcpPort);

            // add to list
            filenames.add(item);


            // Set next filenameSize && nextItem
            nextIndex += filenameSize;
            filenameSize = ByteUtil.byteToInt(bytes[nextIndex]);
            nextIndex++;
        }

        return filenames;
    }

    /**
     * Parses a list of files into a list of limited sized byte array (Each list represents a datagram packet payload).
     *
     * @param files    list of files
     * @param tcpPort  the tcp port to connect
     * @return a list of datagram packet's data.
     */
    public static List<byte[]> parseFileList(File[] files, Integer tcpPort) {

        // Instantiate lists
        LinkedList<String> filenames = getFilenames(files);
        ArrayList<byte[]> packets = new ArrayList<>();
        // Declare data && index
        byte[] data = new byte[Constants.PAYLOAD_SIZE];
        int index = 0;
        // Create header
        byte[] header = createDataHeader(tcpPort);
        index += addBytes(header, data, index);
        // Initialize file count
        int count = 0;
        while (!filenames.isEmpty()) {

            byte[] filenameBytes = filenames.pop().getBytes();
            byte fnLength = ByteUtil.intToByte(filenameBytes.length);

            if ((index + fnLength + 1 /* BYTE FOR FILENAME LENGTH */) > Constants.PAYLOAD_SIZE) {

                // add file count to data && data to packet list
                data[FILE_COUNT_INDEX] = ByteUtil.intToByte(count);
                packets.add(data);
                // Reset variables
                data = new byte[Constants.PAYLOAD_SIZE];
                index = 0;
                // Set data "header"
                index += addBytes(header, data, index);
            }
            // Add file length to data
            data[index] = fnLength;
            index++;
            // Add filename bytes to data
            index += addBytes(filenameBytes, data, index);
            // Increment file count
            count++;
        }

        // add data to list
        data[FILE_COUNT_INDEX] = (new Integer(count)).byteValue();
        packets.add(data);

        return packets;
    }

    /**
     * Obtains a list of filenames from an array of files.
     *
     * @param files the array of files
     * @return a list of filenames from an array of files.
     */
    private static LinkedList<String> getFilenames(File[] files) {

        LinkedList<String> filenames = new LinkedList<>();
        for (File file : files) {

            filenames.add(file.getName());
        }

        return filenames;
    }

    /**
     * Adds an array of bytes into another array of bytes
     *
     * @param toAdd     the array to add
     * @param container the receiver array
     * @param offset    the offset where to index the added array
     * @return number of inserted bytes
     */
    private static int addBytes(byte[] toAdd, byte[] container, int offset) {

        int i;
        for (i = 0; i < toAdd.length; i++) {

            container[i + offset] = toAdd[i];
        }

        return toAdd.length;
    }

    /**
     * Create the application protocol header bytes
     *
     * @param tcpPort the tcp port of the peer
     * @return an array of bytes representing the header
     */
    private static byte[] createDataHeader(Integer tcpPort) {

        String username = Application.settings().getUsername(); // Get username

        byte[] header = new byte[ID_SIZE + VERSION_SIZE + TCP_PORT_SIZE + FILE_COUNT_SIZE + 1 /* username length byte */ + username.length()];
        int index = 0;

        // Add Protocol ID & Version
        header[index] = ByteUtil.intToByte(PROTOCOL_ID);
        index++;
        header[index] = ByteUtil.intToByte(PROTOCOL_VERSION);
        index++;
        // Convert tcp Port
        byte[] portBytes = ByteUtil.intToBytes(tcpPort);
        index += addBytes(portBytes, header, index);
        // Skip file count byte
        index++;
        // Convert username
        header[index] = ByteUtil.intToByte(username.length());
        index++;
        byte[] usrBytes = username.getBytes();
        addBytes(usrBytes, header, index);

        return header;
    }
}
