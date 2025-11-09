package com.tvboot.tivio.media.stream;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.*;

@Slf4j
@Component
public class MulticastStreamTester {

    private static final int DEFAULT_TIMEOUT_MS = 5000;
    private static final int BUFFER_SIZE = 1316; // Standard UDP packet size for IPTV

    /**
     * Test si un flux multicast UDP est disponible
     * @param multicastAddress Adresse multicast (ex: "239.200.0.1")
     * @param port Port UDP (ex: 1234)
     * @param timeoutMs Timeout en millisecondes
     * @return true si le flux envoie des données
     */
    public boolean isStreamAvailable(String multicastAddress, int port, int timeoutMs) {
        MulticastSocket socket = null;

        try {
            log.debug("Testing multicast stream {}:{}", multicastAddress, port);

            // Créer le socket multicast
            InetAddress group = InetAddress.getByName(multicastAddress);
            socket = new MulticastSocket(port);
            socket.setSoTimeout(timeoutMs);

            // Joindre le groupe multicast
            InetSocketAddress groupAddress = new InetSocketAddress(group, port);
            NetworkInterface networkInterface = getDefaultNetworkInterface();
            socket.joinGroup(groupAddress, networkInterface);

            // Buffer pour recevoir les données
            byte[] buffer = new byte[BUFFER_SIZE];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            // Essayer de recevoir un paquet
            socket.receive(packet);

            // Si on arrive ici, le flux est disponible
            log.debug("Stream {}:{} is AVAILABLE - received {} bytes",
                    multicastAddress, port, packet.getLength());

            socket.leaveGroup(groupAddress, networkInterface);
            return true;

        } catch (SocketTimeoutException e) {
            log.warn("Stream {}:{} TIMEOUT - no data received within {}ms",
                    multicastAddress, port, timeoutMs);
            return false;

        } catch (IOException e) {
            log.error("Stream {}:{} ERROR - {}", multicastAddress, port, e.getMessage());
            return false;

        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    /**
     * Test avec timeout par défaut
     */
    public boolean isStreamAvailable(String multicastAddress, int port) {
        return isStreamAvailable(multicastAddress, port, DEFAULT_TIMEOUT_MS);
    }

    /**
     * Obtenir l'interface réseau par défaut pour le multicast
     */
    private NetworkInterface getDefaultNetworkInterface() throws SocketException {
        try {
            // Essayer d'obtenir l'interface par défaut
            return NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
        } catch (Exception e) {
            // Fallback: utiliser la première interface non-loopback
            var interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isUp() && !ni.isLoopback() && ni.supportsMulticast()) {
                    return ni;
                }
            }
            return null;
        }
    }
}