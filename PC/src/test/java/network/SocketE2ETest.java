package network;

import listener.INetworkListener;
import network.handler.receive.TextReceiveHandler;
import network.handler.send.TextSendHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import protocol.PacketHeader;
import protocol.PacketType;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class SocketE2ETest {

    private ServerSocket serverSocket;
    private int port;
    private Socket serverSideSocket;
    private Socket clientSideSocket;
    private final BlockingQueue<String> receivedTexts = new LinkedBlockingQueue<>();
    private volatile boolean serverDone;

    @BeforeEach
    void setUp() throws IOException {
        serverSocket = new ServerSocket(0);
        port = serverSocket.getLocalPort();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (clientSideSocket != null && !clientSideSocket.isClosed()) clientSideSocket.close();
        if (serverSideSocket != null && !serverSideSocket.isClosed()) serverSideSocket.close();
        if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
    }

    @Test
    void textMessageOverTcpShouldBeReceived() throws Exception {
        String message = "Hello over TCP! TCP传输测试。";

        CountDownLatch connectedLatch = new CountDownLatch(1);

        new Thread(() -> {
            try {
                serverSideSocket = serverSocket.accept();
                connectedLatch.countDown();

                DataInputStream dis = new DataInputStream(serverSideSocket.getInputStream());
                PacketHeader header = PacketHeader.readFrom(dis);

                assertEquals(PacketType.TEXT.getValue(), header.getType());

                TextReceiveHandler handler = new TextReceiveHandler();
                handler.handleReceive(header, dis, new INetworkListener() {
                    public void onConnected(String ip) {}
                    public void onDisconnected() { serverDone = true; }
                    public void onTextMessage(String text) { receivedTexts.offer(text); }
                    public void onFileReceived(File f) {}
                    public void onFileSent(File f) {}
                });
            } catch (IOException e) {
                fail("Server error: " + e.getMessage());
            }
        }).start();

        clientSideSocket = new Socket("127.0.0.1", port);
        assertTrue(connectedLatch.await(3, TimeUnit.SECONDS));

        DataOutputStream dos = new DataOutputStream(clientSideSocket.getOutputStream());
        TextSendHandler sender = new TextSendHandler();
        sender.handleSend(message, dos);

        String received = receivedTexts.poll(3, TimeUnit.SECONDS);
        assertNotNull(received, "Should receive text within timeout");
        assertEquals(message, received);
    }

    @Test
    void multipleTextMessagesShouldBeReceivedInOrder() throws Exception {
        CountDownLatch connectedLatch = new CountDownLatch(1);

        new Thread(() -> {
            try {
                serverSideSocket = serverSocket.accept();
                connectedLatch.countDown();

                DataInputStream dis = new DataInputStream(serverSideSocket.getInputStream());
                for (int i = 0; i < 3; i++) {
                    PacketHeader header = PacketHeader.readFrom(dis);
                    TextReceiveHandler handler = new TextReceiveHandler();
                    handler.handleReceive(header, dis, new INetworkListener() {
                        public void onConnected(String ip) {}
                        public void onDisconnected() {}
                        public void onTextMessage(String text) { receivedTexts.offer(text); }
                        public void onFileReceived(File f) {}
                        public void onFileSent(File f) {}
                    });
                }
            } catch (IOException e) {
                fail("Server error: " + e.getMessage());
            }
        }).start();

        clientSideSocket = new Socket("127.0.0.1", port);
        assertTrue(connectedLatch.await(3, TimeUnit.SECONDS));

        DataOutputStream dos = new DataOutputStream(clientSideSocket.getOutputStream());
        TextSendHandler sender = new TextSendHandler();

        for (int i = 1; i <= 3; i++) {
            sender.handleSend("Message " + i, dos);
        }

        for (int i = 1; i <= 3; i++) {
            String received = receivedTexts.poll(3, TimeUnit.SECONDS);
            assertNotNull(received, "Should receive message " + i);
            assertEquals("Message " + i, received);
        }
    }
}
