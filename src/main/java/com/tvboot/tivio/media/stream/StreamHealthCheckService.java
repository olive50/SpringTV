package com.tvboot.tivio.media.stream;

import com.tvboot.tivio.media.stream.dto.StreamHealthResult;
import com.tvboot.tivio.tvchannel.TvChannel;
import com.tvboot.tivio.tvchannel.TvChannelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class StreamHealthCheckService {

    private final TvChannelRepository channelRepository;
    private final MulticastStreamTester streamTester;

    private static final Pattern UDP_PATTERN = Pattern.compile("udp://([0-9.]+):([0-9]+)");

    /**
     * Vérifie un flux multicast individuel
     */
    public StreamHealthResult checkStream(TvChannel channel) {
        long startTime = System.currentTimeMillis();

        StreamHealthResult.StreamHealthResultBuilder resultBuilder = StreamHealthResult.builder()
                .channelId(channel.getId())
                .channelName(channel.getName())
                .channelNumber(channel.getChannelNumber())
                .streamUrl(channel.getWebUrl())
                .lastChecked(LocalDateTime.now());

        try {
            // Parser l'URL UDP
            UdpStreamInfo streamInfo = parseUdpUrl(channel.getWebUrl());

            if (streamInfo == null) {
                return resultBuilder
                        .available(false)
                        .status("ERROR")
                        .message("Invalid stream URL format: " + channel.getWebUrl())
                        .responseTimeMs(System.currentTimeMillis() - startTime)
                        .build();
            }

            resultBuilder
                    .multicastAddress(streamInfo.address)
                    .port(streamInfo.port);

            // Tester la disponibilité
            boolean available = streamTester.isStreamAvailable(
                    streamInfo.address,
                    streamInfo.port,
                    5000
            );

            long responseTime = System.currentTimeMillis() - startTime;

            return resultBuilder
                    .available(available)
                    .status(available ? "ONLINE" : "OFFLINE")
                    .message(available ? "Stream is broadcasting" : "No data received from stream")
                    .responseTimeMs(responseTime)
                    .build();

        } catch (Exception e) {
            log.error("Error checking stream for channel {}: {}", channel.getName(), e.getMessage());

            return resultBuilder
                    .available(false)
                    .status("ERROR")
                    .message("Error: " + e.getMessage())
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    /**
     * Vérifie tous les flux actifs
     */
    @Transactional(readOnly = true)
    public List<StreamHealthResult> checkAllStreams() {
        log.info("Starting health check for all active TV channels");

        List<TvChannel> activeChannels = channelRepository.findByActiveTrue();
        List<StreamHealthResult> results = new ArrayList<>();

        for (TvChannel channel : activeChannels) {
            StreamHealthResult result = checkStream(channel);
            results.add(result);
        }

        log.info("Health check completed: {} channels checked", results.size());
        return results;
    }

    /**
     * 🔥 SCHEDULED TASK - Vérifie tous les flux toutes les 5 minutes
     * ✅ S'active UNIQUEMENT si app.stream.health-check.enabled=true
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    @Transactional
    @ConditionalOnProperty(
            value = "app.stream.health-check.enabled",
            havingValue = "true",
            matchIfMissing = false  // ⚠️ false = désactivé par défaut
    )
    public void scheduledStreamHealthCheck() {
        log.info("=== SCHEDULED STREAM HEALTH CHECK STARTED ===");

        List<TvChannel> activeChannels = channelRepository.findByActiveTrue();
        int totalChannels = activeChannels.size();
        int availableCount = 0;
        int unavailableCount = 0;

        for (TvChannel channel : activeChannels) {
            try {
                StreamHealthResult result = checkStream(channel);

                if (!channel.getAvailable().equals(result.getAvailable())) {
                    channel.setAvailable(result.getAvailable());
                    channelRepository.save(channel);

                    log.warn("Channel {} status changed to: {}",
                            channel.getName(),
                            result.getAvailable() ? "AVAILABLE" : "UNAVAILABLE");
                }

                if (result.getAvailable()) {
                    availableCount++;
                } else {
                    unavailableCount++;
                }

            } catch (Exception e) {
                log.error("Error during scheduled check for channel {}: {}",
                        channel.getName(), e.getMessage());
                unavailableCount++;
            }
        }

        log.info("=== SCHEDULED CHECK COMPLETED: {}/{} available, {}/{} unavailable ===",
                availableCount, totalChannels, unavailableCount, totalChannels);
    }

    /**
     * 🔥 ALTERNATIVE: Vérification asynchrone rapide (toutes les 2 minutes)
     * ✅ S'active UNIQUEMENT si app.stream.health-check.fast-check.enabled=true
     */
    @Scheduled(fixedRate = 120000, initialDelay = 30000)
    @Async
    @Transactional
    @ConditionalOnProperty(
            value = "app.stream.health-check.fast-check.enabled",
            havingValue = "true",
            matchIfMissing = false
    )
    public void fastStreamHealthCheck() {
        log.info("=== FAST STREAM CHECK STARTED ===");

        List<TvChannel> channels = channelRepository.findByActiveTrueAndAvailableTrueOrderBySortOrderAscNameAsc(
                org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)
        ).getContent();

        List<CompletableFuture<StreamHealthResult>> futures = new ArrayList<>();

        for (TvChannel channel : channels) {
            CompletableFuture<StreamHealthResult> future = CompletableFuture.supplyAsync(() ->
                    checkStream(channel)
            );
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        log.info("=== FAST CHECK COMPLETED ===");
    }

    /**
     * Parser une URL UDP multicast
     */
    private UdpStreamInfo parseUdpUrl(String streamUrl) {
        if (streamUrl == null || streamUrl.trim().isEmpty()) {
            return null;
        }

        Matcher matcher = UDP_PATTERN.matcher(streamUrl);
        if (matcher.find()) {
            try {
                String address = matcher.group(1);
                int port = Integer.parseInt(matcher.group(2));
                return new UdpStreamInfo(address, port);
            } catch (NumberFormatException e) {
                log.error("Invalid port in stream URL: {}", streamUrl);
                return null;
            }
        }

        return null;
    }

    /**
     * Classe interne pour stocker les infos de stream UDP
     */
    private static class UdpStreamInfo {
        final String address;
        final int port;

        UdpStreamInfo(String address, int port) {
            this.address = address;
            this.port = port;
        }
    }
}