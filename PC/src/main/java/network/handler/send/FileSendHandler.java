package network.handler.send;

import protocol.PacketHeader;
import protocol.PacketType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import utils.CryptoUtils;
import static utils.FileTypeUtils.getTypeByFile;

public class FileSendHandler implements ISendHandler<File>{

    @Override
    public void handleSend(File file, DataOutputStream dos) throws IOException {
        if(!file.exists()){
            return;
        }
        String fileName = file.getName();
        byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);
        // long fileNameLength = fileName.length();
        short nameLength = (short) fileNameBytes.length;
        long filelength = file.length();
        PacketType fileType = getTypeByFile(file);

        byte[] fileBytes = Files.readAllBytes(file.toPath());

        long bodyLength = nameLength + filelength;

        PacketHeader header = new PacketHeader(
                fileType.getValue(),
                //(short) fileNameLength,
                nameLength,
                //filelength,
                bodyLength,
                0,
                filelength
        );

        header.writeTo(dos);
        dos.write(fileNameBytes);
        dos.write(CryptoUtils.xor(fileBytes));
        dos.flush();
    }
}
