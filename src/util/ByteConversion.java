package util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Util class to convert bytes.
 * <p>
 * Created by danielGoncalves on 11/05/17.
 */
public final class ByteConversion {

    /**
     * The caller references the ByteConversion using <tt>ByteConversion.EMPTY_STRING</tt>,
     * and so on. Thus, the caller should be prevented from constructing objects of
     * this class, by declaring this private constructor.
     */
    private ByteConversion() {
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

    public static short bytesToShort(byte[] bytes) {
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    public static byte[] shortToBytes(short value) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value).array();
    }
}
