package client.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class DiscoveryService {
    private static final int UDP_PORT = 12346;
    private static final String DISCOVERY_MESSAGE = "GAME_HOST_DISCOVERY";
    
    private Thread responderThread;
    private volatile boolean running = false;

    public interface DiscoveryListener {
        void onHostDiscovered(String hostIP);
    }

    public List<String> discoverHosts() {
        List<String> hosts = new ArrayList<>();
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.setSoTimeout(1000);
            byte[] message = DISCOVERY_MESSAGE.getBytes();
            DatagramPacket packet = new DatagramPacket(
                message, message.length, InetAddress.getByName("255.255.255.255"), UDP_PORT
            );
            socket.send(packet);
            byte[] buffer = new byte[256];
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < 1000) {
                try {
                    DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
                    socket.receive(responsePacket);
                    String hostIP = new String(responsePacket.getData(), 0, responsePacket.getLength()).trim();
                    if (!hosts.contains(hostIP)) hosts.add(hostIP);
                } catch (SocketTimeoutException e) { break; }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return hosts;
    }

    public void startResponding(String localIP) {
        running = true;
        responderThread = new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(UDP_PORT)) {
                byte[] buffer = new byte[256];
                while (running && !socket.isClosed()) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength());
                    if (DISCOVERY_MESSAGE.equals(message)) {
                        byte[] response = localIP.getBytes();
                        DatagramPacket responsePacket = new DatagramPacket(
                            response, response.length, packet.getAddress(), packet.getPort()
                        );
                        socket.send(responsePacket);
                    }
                }
            } catch (IOException e) {
                if (running) e.printStackTrace();
            }
        });
        responderThread.setDaemon(true);
        responderThread.start();
    }

    public void stop() {
        running = false;
        if (responderThread != null) {
            responderThread.interrupt();
        }
    }
}