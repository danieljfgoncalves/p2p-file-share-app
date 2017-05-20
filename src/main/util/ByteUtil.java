package util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Util class to convert bytes.
 * <p>
 * Created by 2DD - Group SNOW WHITE {1151452, 1151031, 1141570, 1151088}
 */
public final class ByteUtil {

    private static final int INTEGER_SIZE = Integer.SIZE / Byte.SIZE;

    /**
     * The caller references the ByteUtil using <tt>ByteUtil.EMPTY_STRING</tt>,
     * and so on. Thus, the caller should be prevented from constructing objects of
     * this class, by declaring this private constructor.
     */
    private ByteUtil() {
        //this prevents even the native class from
        //calling this ctor as well :
        throw new AssertionError();
    }

    /**
     * Converts a byte to an int
     *
     * @param value the byte to convert
     * @return the converted int
     */
    public static int byteToInt(byte value) {

        return (new Byte(value)).intValue();
    }

    /**
     * Converts an int to a byte
     *
     * @param value the int to convert
     * @return the converted byte
     */
    public static byte intToByte(int value) {

        return (new Integer(value)).byteValue();
    }

    /**
     * Converts an array of bytes to an int
     *
     * @param value the array of bytes
     * @return the converted int
     */
    public static int bytesToInt(byte[] value) {

        return ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    /**
     * Converts an int to an array of bytes
     *
     * @param value the int to converted
     * @return the converted byte array
     */
    public static byte[] intToBytes(int value) {

        return ByteBuffer.allocate(INTEGER_SIZE).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
    }

    /**
     * Creates a string with a human readable scale to represent a size in bytes (ex. 5 MB or 6 kB)
     *
     * @param bytes the size in bytes to convert
     * @param si    the SI unit to use (Standard: 1000 bytes vs Binary 1024)
     * @return the string representation of the size
     */
    public static String readableByteCount(long bytes, boolean si) {

        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", (bytes / Math.pow(unit, exp)), pre);
    }
}
