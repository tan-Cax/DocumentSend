package network;

import org.junit.jupiter.api.Test;
import protocol.PacketHeader;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class PacketHeaderTest {

    @Test
    void headerLengthConstantShouldBe29() {
        assertEquals(29, PacketHeader.HEADER_LENGTH);
    }

    @Test
    void defaultConstructorShouldSetZeroes() {
        PacketHeader h = new PacketHeader();
        assertEquals(0, h.getType());
        assertEquals(0, h.getNameLength());
        assertEquals(0, h.getBodyLength());
        assertEquals(0, h.getOffset());
        assertEquals(0, h.getTotalLength());
    }

    @Test
    void parameterizedConstructorShouldSetFields() {
        PacketHeader h = new PacketHeader((byte) 4, (short) 10, 2048L, 512L, 8192L);
        assertEquals(4, h.getType());
        assertEquals(10, h.getNameLength());
        assertEquals(2048, h.getBodyLength());
        assertEquals(512, h.getOffset());
        assertEquals(8192, h.getTotalLength());
    }

    @Test
    void writeToAndReadFromShouldRoundtrip() throws IOException {
        PacketHeader original = new PacketHeader((byte) 2, (short) 7, 123456L, 0L, 999999L);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        original.writeTo(new DataOutputStream(baos));

        PacketHeader parsed = PacketHeader.readFrom(new DataInputStream(new ByteArrayInputStream(baos.toByteArray())));

        assertEquals(original.getType(), parsed.getType());
        assertEquals(original.getNameLength(), parsed.getNameLength());
        assertEquals(original.getBodyLength(), parsed.getBodyLength());
        assertEquals(original.getOffset(), parsed.getOffset());
        assertEquals(original.getTotalLength(), parsed.getTotalLength());
    }

    @Test
    void readFromShouldThrowOnMagicMismatch() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeShort(0xBEEF);
        dos.writeByte(1);
        dos.writeShort(0);
        dos.writeLong(0);
        dos.writeLong(0);
        dos.writeLong(0);

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
        assertThrows(IOException.class, () -> PacketHeader.readFrom(dis));
    }

    @Test
    void settersShouldUpdateFields() {
        PacketHeader h = new PacketHeader();
        h.setType((byte) 3);
        h.setNameLength((short) 5);
        h.setBodyLength(100L);
        h.setOffset(50L);
        h.setTotalLength(200L);

        assertEquals(3, h.getType());
        assertEquals(5, h.getNameLength());
        assertEquals(100, h.getBodyLength());
        assertEquals(50, h.getOffset());
        assertEquals(200, h.getTotalLength());
    }

    @Test
    void writeToShouldProduceExactly29Bytes() throws IOException {
        PacketHeader h = new PacketHeader((byte) 1, (short) 4, 256L, 0L, 256L);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        h.writeTo(new DataOutputStream(baos));
        assertEquals(29, baos.size());
    }

    @Test
    void toStringShouldContainFields() {
        PacketHeader h = new PacketHeader((byte) 1, (short) 0, 10L, 0L, 10L);
        String s = h.toString();
        assertTrue(s.contains("type=1"));
        assertTrue(s.contains("bodyLength=10"));
    }
}
