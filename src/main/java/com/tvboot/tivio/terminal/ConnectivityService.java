package com.tvboot.tivio.terminal;

import com.tvboot.tivio.terminal.dto.ConnectivityTestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// src/main/java/com/tvboot/iptv/service/ConnectivityService.java
@Service
@Slf4j
public class ConnectivityService {

    private static final int TIMEOUT_MS = 5000;
    private static final int PORT = 80; // Default port for connectivity test

    public ConnectivityTestResult pingHost(String ipAddress) {
        long startTime = System.currentTimeMillis();

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ipAddress, PORT), TIMEOUT_MS);


            return ConnectivityTestResult.builder()
                    .success(true)
                    .message("Host is reachable")

                    .timestamp(LocalDateTime.now())
                    .details(Map.of(
                            "method", "TCP_CONNECT",
                            "port", PORT,
                            "host", ipAddress
                    ))
                    .build();

        } catch (IOException e) {
            // Try ICMP ping as fallback
            return performIcmpPing(ipAddress, startTime);
        }
    }

    private ConnectivityTestResult performIcmpPing(String ipAddress, long startTime) {
        try {
            InetAddress inet = InetAddress.getByName(ipAddress);
            boolean reachable = inet.isReachable(TIMEOUT_MS);
            long responseTime = System.currentTimeMillis() - startTime;

            if (reachable) {
                return ConnectivityTestResult.builder()
                        .success(true)
                        .message("Host is reachable via ICMP")

                        .timestamp(LocalDateTime.now())
                        .details(Map.of(
                                "method", "ICMP_PING",
                                "host", ipAddress
                        ))
                        .build();
            } else {
                return ConnectivityTestResult.builder()
                        .success(false)
                        .message("Host is not reachable")

                        .timestamp(LocalDateTime.now())
                        .errorCode("HOST_UNREACHABLE")
                        .details(Map.of(
                                "method", "ICMP_PING",
                                "host", ipAddress
                        ))
                        .build();
            }

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Ping failed for {}: {}", ipAddress, e.getMessage());

            return ConnectivityTestResult.builder()
                    .success(false)
                    .message("Connectivity test failed: " + e.getMessage())

                    .timestamp(LocalDateTime.now())
                    .errorCode("PING_FAILED")
                    .details(Map.of(
                            "method", "ICMP_PING",
                            "host", ipAddress,
                            "error", e.getMessage()
                    ))
                    .build();
        }
    }

    public ConnectivityTestResult testTerminalServices(String ipAddress) {
        // Test multiple services commonly used by IPTV terminals
        Map<String, Boolean> serviceTests = new HashMap<>();
        long startTime = System.currentTimeMillis();

        // Test common IPTV terminal ports
        int[] portsToTest = {80, 443, 8080, 554, 1935}; // HTTP, HTTPS, Alt HTTP, RTSP, RTMP

        boolean anyServiceReachable = false;
        for (int port : portsToTest) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(ipAddress, port), 2000);
                serviceTests.put("port_" + port, true);
                anyServiceReachable = true;
            } catch (IOException e) {
                serviceTests.put("port_" + port, false);
            }
        }

        long responseTime = System.currentTimeMillis() - startTime;

        return ConnectivityTestResult.builder()
                .success(anyServiceReachable)
                .message(anyServiceReachable ?
                        "Terminal services are accessible" :
                        "No terminal services are accessible")

                .timestamp(LocalDateTime.now())
                .details(Map.of(
                        "method", "SERVICE_TEST",
                        "host", ipAddress,
                        "services", serviceTests
                ))
                .build();
    }
}