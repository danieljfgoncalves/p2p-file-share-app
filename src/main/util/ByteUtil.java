package util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Util class to convert bytes.
 * <p>
 * Created by danielGoncalves on 11/05/17.
 */
public final class ByteUtil {

    private static final int INTEGER_SIZE = Integer.SIZE / Byte.SIZE;
    private static final int SHORT_SIZE = Short.SIZE / Byte.SIZE;

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

    public static int byteToInt(byte value) {

        return (new Byte(value)).intValue();
    }

    public static byte intToByte(int value) {

        return (new Integer(value)).byteValue();
    }

    public static int bytesToInt(byte[] value) {

        return ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public static byte[] intToBytes(int value) {

        return ByteBuffer.allocate(INTEGER_SIZE).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
    }

    public static short bytesToShort(byte[] value) {
        return ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    public static byte[] shortToBytes(short value) {
        return ByteBuffer.allocate(SHORT_SIZE).order(ByteOrder.LITTLE_ENDIAN).putShort(value).array();
    }
}
