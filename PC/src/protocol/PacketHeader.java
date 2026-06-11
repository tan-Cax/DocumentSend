package protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketHeader {
    public static final short MAGIC_NUMBER = (short) 0xCAFE;
    public static final int HEADER_LENGTH = 29;

    private byte type;
    private short nameLength;
    private long bodyLength;
    private long offset;
    private long totalLength;

    // 默认构造
    public PacketHeader() {
    }

    public PacketHeader(byte type, short nameLength, long bodyLength, long offset, long totalLength) {
        this.type = type;
        this.nameLength = nameLength;
        this.bodyLength = bodyLength;
        this.offset = offset;
        this.totalLength = totalLength;
    }

    /**
     * 从输入流中读取 29 字节并解析为报头对象
     */
    public static PacketHeader readFrom(DataInputStream dis) throws IOException {
        short magic = dis.readShort();
        if (magic != MAGIC_NUMBER) {
            throw new IOException("Magic Number mismatch: expected 0xCAFE, got 0x" + Integer.toHexString(magic & 0xFFFF));
        }

        PacketHeader header = new PacketHeader();
        header.type = dis.readByte();
        header.nameLength = dis.readShort();
        header.bodyLength = dis.readLong();
        header.offset = dis.readLong();
        header.totalLength = dis.readLong();
        return header;
    }

    /**
     * 将当前报头数据写入输出流 (预留给发送方使用)
     */
    public void writeTo(DataOutputStream dos) throws IOException {
        dos.writeShort(MAGIC_NUMBER);
        dos.writeByte(type);
        dos.writeShort(nameLength);
        dos.writeLong(bodyLength);
        dos.writeLong(offset);
        dos.writeLong(totalLength);
    }

    // --- Getters ---
    public byte getType() { return type; }
    public short getNameLength() { return nameLength; }
    public long getBodyLength() { return bodyLength; }
    public long getOffset() { return offset; }
    public long getTotalLength() { return totalLength; }

    // --- Setters ---
    public void setType(byte type) { this.type = type; }
    public void setNameLength(short nameLength) { this.nameLength = nameLength; }
    public void setBodyLength(long bodyLength) { this.bodyLength = bodyLength; }
    public void setOffset(long offset) { this.offset = offset; }
    public void setTotalLength(long totalLength) { this.totalLength = totalLength; }
    
    @Override
    public String toString() {
        return "PacketHeader{" +
                "type=" + type +
                ", nameLength=" + nameLength +
                ", bodyLength=" + bodyLength +
                ", offset=" + offset +
                ", totalLength=" + totalLength +
                '}';
    }
}
