package tests.domain;

import domain.RemoteFilename;
import domain.RemoteFilenameList;
import networking.RemoteFilenameListProtocol;
import settings.Application;

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
public class RemoteFilenameListProtocolTest {

    private File[] files;
    private String username;
    private InetAddress addr;
    private Integer tcpPort;

    @org.junit.Before
    public void setUp() throws Exception {

        username = Application.settings().getUsername();
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
        RemoteFilenameList fnSet1 = new RemoteFilenameList();
        fnSet1.add(new RemoteFilename("file1", username, addr, tcpPort));
        fnSet1.add(new RemoteFilename("file2", username, addr, tcpPort));
        fnSet1.add(new RemoteFilename("file3", username, addr, tcpPort));
        fnSet1.add(new RemoteFilename("file4", username, addr, tcpPort));
        fnSet1.add(new RemoteFilename("file5", username, addr, tcpPort));

        List<byte[]> bytes = RemoteFilenameListProtocol.parseFileList(files, tcpPort);

        RemoteFilenameList fnSet2 = new RemoteFilenameList();

        for (byte[] packet :
                bytes) {
            fnSet2.addAll(RemoteFilenameListProtocol.parsePacket(packet, addr));
        }

        assertThat(fnSet1, is(fnSet2));
    }
}