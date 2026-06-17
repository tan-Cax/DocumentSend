package utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class NetworkUtils {

    /**
     * 获取本机非回环的 IPv4 地址
     */
    public static InetAddress getLocalIpv4Address() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isLoopback() || !ni.isUp()) continue;
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address) {
                        return addr;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<InetAddress> getBroadcastAddresses() {
        List<InetAddress> result = new ArrayList<>();

        try {
            Enumeration<NetworkInterface> interfaces =
                    NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();

                if (!ni.isUp()
                        || ni.isLoopback()
                        || ni.isVirtual()) {
                    continue;
                }

                for (InterfaceAddress ia : ni.getInterfaceAddresses()) {

                    InetAddress broadcast = ia.getBroadcast();

                    if (broadcast != null) {
                        result.add(broadcast);

                        System.out.println(
                                "[UDP] 发现广播地址: "
                                        + broadcast.getHostAddress()
                        );
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private static byte[] prefixLengthToMask(short prefixLength) {
        byte[] mask = new byte[4];
        int fullBytes = prefixLength / 8;
        int remainingBits = prefixLength % 8;
        for (int i = 0; i < fullBytes; i++) {
            mask[i] = (byte) 0xFF;
        }
        if (fullBytes < 4 && remainingBits > 0) {
            mask[fullBytes] = (byte) (0xFF << (8 - remainingBits));
        }
        return mask;
    }
}
