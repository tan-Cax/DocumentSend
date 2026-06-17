package network;

import listener.INetworkListener;
import network.handler.receive.FileReceiveHandler;
import network.handler.send.FileSendHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import protocol.PacketHeader;
import utils.AppConfig;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class FileTransferHandlerTest {

    private String originalSaveDir;

    @BeforeEach
    void setUp() {
        originalSaveDir = AppConfig.getSaveDir();
    }

    @AfterEach
    void tearDown() {
        AppConfig.setSaveDir(originalSaveDir);
    }

    @Test
    void sendAndReceiveTextFile(@TempDir Path tempDir) throws IOException {
        AppConfig.setSaveDir(tempDir.toString());

        Path srcPath = tempDir.resolve("hello.txt");
        String content = "Hello, file transfer! 文件传输测试内容。";
        Files.writeString(srcPath, content, StandardCharsets.UTF_8);
        File srcFile = srcPath.toFile();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        FileSendHandler sender = new FileSendHandler();
        sender.handleSend(srcFile, dos);

        byte[] wire = baos.toByteArray();
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(wire));
        PacketHeader header = PacketHeader.readFrom(dis);

        assertEquals(srcFile.getName().getBytes(StandardCharsets.UTF_8).length, header.getNameLength());
        assertEquals(header.getNameLength() + srcFile.length(), header.getBodyLength());

        AtomicReference<File> captured = new AtomicReference<>();
        INetworkListener listener = new INetworkListener() {
            public void onConnected(String ip) {}
            public void onDisconnected() {}
            public void onTextMessage(String text) {}
            public void onFileReceived(File f) { captured.set(f); }
            public void onFileSent(File f) {}
        };

        FileReceiveHandler receiver = new FileReceiveHandler();
        receiver.handleReceive(header, dis, listener);

        assertNotNull(captured.get());
        assertTrue(captured.get().exists());
        byte[] receivedBytes = Files.readAllBytes(captured.get().toPath());
        assertEquals(content, new String(receivedBytes, StandardCharsets.UTF_8));
    }

    @Test
    void sendAndReceiveBinaryFile(@TempDir Path tempDir) throws IOException {
        AppConfig.setSaveDir(tempDir.toString());

        Path srcPath = tempDir.resolve("data.bin");
        byte[] binaryData = new byte[20000];
        for (int i = 0; i < binaryData.length; i++) {
            binaryData[i] = (byte) (i & 0xFF);
        }
        Files.write(srcPath, binaryData);
        File srcFile = srcPath.toFile();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        FileSendHandler sender = new FileSendHandler();
        sender.handleSend(srcFile, dos);

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
        PacketHeader header = PacketHeader.readFrom(dis);

        assertEquals(header.getNameLength() + srcFile.length(), header.getBodyLength());

        AtomicReference<File> captured = new AtomicReference<>();
        INetworkListener listener = new INetworkListener() {
            public void onConnected(String ip) {}
            public void onDisconnected() {}
            public void onTextMessage(String text) {}
            public void onFileReceived(File f) { captured.set(f); }
            public void onFileSent(File f) {}
        };

        FileReceiveHandler receiver = new FileReceiveHandler();
        receiver.handleReceive(header, dis, listener);

        assertNotNull(captured.get());
        byte[] receivedBytes = Files.readAllBytes(captured.get().toPath());
        assertArrayEquals(binaryData, receivedBytes);
    }

    @Test
    void receiveShouldHandleDuplicateFilename(@TempDir Path tempDir) throws IOException {
        AppConfig.setSaveDir(tempDir.toString());

        Path existingPath = tempDir.resolve("dup.txt");
        Files.writeString(existingPath, "existing", StandardCharsets.UTF_8);

        String content = "new file";
        Path srcPath = tempDir.resolve("dup.txt");
        Files.writeString(srcPath, content, StandardCharsets.UTF_8);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        FileSendHandler sender = new FileSendHandler();
        sender.handleSend(srcPath.toFile(), dos);

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
        PacketHeader header = PacketHeader.readFrom(dis);

        AtomicReference<File> captured = new AtomicReference<>();
        INetworkListener listener = new INetworkListener() {
            public void onConnected(String ip) {}
            public void onDisconnected() {}
            public void onTextMessage(String text) {}
            public void onFileReceived(File f) { captured.set(f); }
            public void onFileSent(File f) {}
        };

        FileReceiveHandler receiver = new FileReceiveHandler();
        receiver.handleReceive(header, dis, listener);

        File received = captured.get();
        assertNotNull(received);
        assertEquals("dup(1).txt", received.getName());
        assertEquals(content, Files.readString(received.toPath(), StandardCharsets.UTF_8));
    }

    @Test
    void senderShouldSkipNonExistentFile(@TempDir Path tempDir) throws IOException {
        File missing = tempDir.resolve("nonexistent.dat").toFile();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        FileSendHandler sender = new FileSendHandler();
        sender.handleSend(missing, dos);

        assertEquals(0, baos.size(), "Should not write anything for missing file");
    }
}
