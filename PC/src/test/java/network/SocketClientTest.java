package network;

import org.junit.jupiter.api.Test;
import protocol.PacketType;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class SocketClientTest {

    @Test
    void sendTextWithoutConnectionShouldNotThrow() {
        SocketClient client = new SocketClient();
        assertDoesNotThrow(() -> client.sendText("test without connection"));
    }

    @Test
    void sendFileWithoutConnectionShouldNotThrow() {
        SocketClient client = new SocketClient();
        assertDoesNotThrow(() -> client.sendFile(new File("nonexistent.txt")));
    }

    @Test
    void sendTextToWithInvalidTargetShouldNotThrow() {
        SocketClient client = new SocketClient();
        assertDoesNotThrow(() -> client.sendTextTo("test", "127.0.0.1", 1));
    }

    @Test
    void sendFileToWithInvalidTargetShouldNotThrow() {
        SocketClient client = new SocketClient();
        assertDoesNotThrow(() -> client.sendFileTo(new File("test.txt"), "127.0.0.1", 1));
    }
}
