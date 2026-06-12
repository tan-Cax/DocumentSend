package protocol;

public enum PacketType {
    TEXT((byte) 1),
    IMAGE((byte) 2),
    VIDEO((byte) 3),
    FILE((byte) 4),
    ARCHIVE((byte) 5);

    private final byte value;

    PacketType(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public static PacketType fromValue(byte value) {
        for (PacketType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return null;
    }
}
