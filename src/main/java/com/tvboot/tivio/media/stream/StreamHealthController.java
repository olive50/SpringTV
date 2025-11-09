package com.tvboot.tivio.media.stream;

import com.tvboot.tivio.common.dto.respone.TvBootHttpResponse;
import com.tvboot.tivio.media.stream.dto.StreamHealthResult;
import com.tvboot.tivio.tvchannel.TvChannel;
import com.tvboot.tivio.tvchannel.TvChannelRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/streams")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "📡 Stream Health", description = "Vérification de disponibilité des flux IPTV")
public class StreamHealthController {

    private final StreamHealthCheckService healthCheckService;
    private final TvChannelRepository channelRepository;

    @GetMapping("/health/all")
    @Operation(summary = "Vérifier tous les flux", description = "Teste la disponibilité de tous les flux actifs")
    public ResponseEntity<TvBootHttpResponse> checkAllStreams() {
        log.info("Manual health check requested for all streams");

        try {
            List<StreamHealthResult> results = healthCheckService.checkAllStreams();

            long availableCount = results.stream()
                    .filter(StreamHealthResult::getAvailable)
                    .count();

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Stream health check completed")
                    .build()
                    .addData("results", results)
                    .addData("totalChannels", results.size())
                    .addData("availableChannels", availableCount)
                    .addData("unavailableChannels", results.size() - availableCount);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error during stream health check", e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Stream health check failed",
                    e.getMessage()
            );
        }
    }
/*
    @GetMapping("/health/channel/{channelId}")
    @Operation(summary = "Vérifier un flux spécifique", description = "Teste la disponibilité d'un flux par ID de chaîne")
    public ResponseEntity<TvBootHttpResponse> checkChannelStream(@PathVariable Long channelId) {
        log.info("Stream health check requested for channel ID: {}", channelId);

        try {
            TvChannel channel = channelRepository.findById(channelId)
                    .orElseThrow(() -> new RuntimeException("Channel not found: " + channelId));

            StreamHealthResult result = healthCheckService.checkStream(channel);

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Stream health check completed for channel: " + channel.getName())
                    .build()
                    .addData("result", result);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error checking stream for channel {}", channelId, e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Stream health check failed",
                    e.getMessage()
            );
        }
    }
 */
    @GetMapping("/health/unavailable")
    @Operation(summary = "Lister les flux indisponibles", description = "Retourne uniquement les chaînes dont le flux est DOWN")
    public ResponseEntity<TvBootHttpResponse> getUnavailableStreams() {
        log.info("Requesting list of unavailable streams");

        try {
            List<StreamHealthResult> allResults = healthCheckService.checkAllStreams();

            List<StreamHealthResult> unavailable = allResults.stream()
                    .filter(result -> !result.getAvailable())
                    .toList();

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Unavailable streams retrieved")
                    .build()
                    .addData("unavailableStreams", unavailable)
                    .addCount(unavailable.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting unavailable streams", e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Failed to get unavailable streams",
                    e.getMessage()
            );
        }
    }

    @GetMapping("/health/channel/{channelId}")
    @Operation(summary = "Vérifier un flux spécifique", description = "Teste la disponibilité d'un flux par ID de chaîne")
    public ResponseEntity<TvBootHttpResponse> checkChannelStream(
            @PathVariable Long channelId,
            @RequestParam(defaultValue = "true") boolean updateDatabase) {  // ← Nouveau paramètre

        log.info("Stream health check requested for channel ID: {}", channelId);

        try {
            TvChannel channel = channelRepository.findById(channelId)
                    .orElseThrow(() -> new RuntimeException("Channel not found: " + channelId));

            StreamHealthResult result = healthCheckService.checkStream(channel);

            // ✅ Mettre à jour la base de données si demandé
            if (updateDatabase && !channel.getAvailable().equals(result.getAvailable())) {
                channel.setAvailable(result.getAvailable());
                channelRepository.save(channel);

                log.info("Channel {} availability updated to: {}",
                        channel.getName(), result.getAvailable());
            }

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Stream health check completed for channel: " + channel.getName())
                    .build()
                    .addData("result", result);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error checking stream for channel {}", channelId, e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Stream health check failed",
                    e.getMessage()
            );
        }
    }
}