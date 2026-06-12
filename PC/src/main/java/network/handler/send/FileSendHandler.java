package network.handler.send;

import protocol.PacketHeader;
import protocol.PacketType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static utils.FileTypeUtils.getTypeByFile;

public class FileSendHandler implements ISendHandler<File>{

    @Override
    public void handleSend(File file, DataOutputStream dos) throws IOException {
        if(!file.exists()){
            return;
        }
        String fileName = file.getName();
        long fileNameLength = fileName.length();
        byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);
        long filelength = file.length();
        PacketType fileType = getTypeByFile(file);

        byte[] fileBytes = Files.readAllBytes(file.toPath());

        PacketHeader header = new PacketHeader(
                fileType.getValue(),
                (short) fileNameLength,
                filelength,
                0,0
        );

        header.writeTo(dos);
        dos.write(fileNameBytes);
        dos.write(fileBytes);
        dos.flush();
    }
}
