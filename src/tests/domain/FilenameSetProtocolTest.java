package tests.domain;

import domain.FilenameItem;
import domain.FilenameItemSet;
import domain.FilenameSetProtocol;

import java.io.File;
import java.net.InetAddress;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests a filename set protocol (UDP)
 * <p>
 * Created by danielGoncalves on 10/05/17.
 */
public class FilenameSetProtocolTest {

    private File[] files;
    private String username;
    private InetAddress addr;
    private short tcpPort;

    @org.junit.Before
    public void setUp() throws Exception {

        username = "User1";
        addr = InetAddress.getByName("127.0.0.1");
        tcpPort = 8888;

        files = new File[5];
        files[0] = new File("/file1");
        files[1] = new File("/file2");
        files[2] = new File("/file3");
        files[3] = new File("/file4");
        files[4] = new File("/file5");
    }

    @org.junit.Test
    public void ensureSetIsEqualAfterParsing() throws Exception {

        System.out.println("ensureSetIsEqualAfterParsing");
        FilenameItemSet fnSet1 = new FilenameItemSet();
        fnSet1.add(new FilenameItem("file1", username, addr, tcpPort));
        fnSet1.add(new FilenameItem("file2", username, addr, tcpPort));
        fnSet1.add(new FilenameItem("file3", username, addr, tcpPort));
        fnSet1.add(new FilenameItem("file4", username, addr, tcpPort));
        fnSet1.add(new FilenameItem("file5", username, addr, tcpPort));

        List<byte[]> bytes = FilenameSetProtocol.parseFileList(files, tcpPort, username);

        FilenameItemSet fnSet2 = new FilenameItemSet();

        for (byte[] packet :
                bytes) {
            FilenameSetProtocol.parsePacket(fnSet2, packet, addr);
        }

        assertThat(fnSet1, is(fnSet2));
    }
}