package network;

import org.junit.jupiter.api.Test;
import protocol.PacketType;

import static org.junit.jupiter.api.Assertions.*;

class PacketTypeTest {

    @Test
    void enumValuesShouldMatchSpec() {
        assertEquals(1, PacketType.TEXT.getValue());
        assertEquals(2, PacketType.IMAGE.getValue());
        assertEquals(3, PacketType.VIDEO.getValue());
        assertEquals(4, PacketType.FILE.getValue());
        assertEquals(5, PacketType.ARCHIVE.getValue());
    }

    @Test
    void fromValueShouldReturnCorrectEnum() {
        assertEquals(PacketType.TEXT, PacketType.fromValue((byte) 1));
        assertEquals(PacketType.IMAGE, PacketType.fromValue((byte) 2));
        assertEquals(PacketType.VIDEO, PacketType.fromValue((byte) 3));
        assertEquals(PacketType.FILE, PacketType.fromValue((byte) 4));
        assertEquals(PacketType.ARCHIVE, PacketType.fromValue((byte) 5));
    }

    @Test
    void fromValueShouldReturnNullForUnknown() {
        assertNull(PacketType.fromValue((byte) 0));
        assertNull(PacketType.fromValue((byte) 99));
        assertNull(PacketType.fromValue((byte) -1));
    }

    @Test
    void enumCountShouldBeFive() {
        assertEquals(5, PacketType.values().length);
    }
}
