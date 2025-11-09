package com.tvboot.tivio.wifi;

import com.google.zxing.WriterException;
import com.tvboot.tivio.terminal.Terminal;
import com.tvboot.tivio.wifi.dto.AccessPointCreateRequest;
import com.tvboot.tivio.wifi.dto.AccessPointDTO;
import com.tvboot.tivio.wifi.dto.AccessPointUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/wifi")
@Slf4j
@RequiredArgsConstructor
public class WifiController {

    private final WifiQrCodeService qrCodeService;
    private final AccessPointRepository accessPointRepository;

    @GetMapping("/{terminalId}")
    public ResponseEntity<AccessPointDTO> getAccessPoint(@PathVariable Long terminalId) {
        AccessPoint accessPoint = accessPointRepository
                .findByTerminalId(terminalId)
                .orElseThrow(() -> new RuntimeException("Access point not found for terminal: " + terminalId));

        AccessPointDTO dto = AccessPointDTO.builder()
                .id(accessPoint.getId())
                .ssid(accessPoint.getSsid())
                .password(accessPoint.getPassword())
                .securityProtocol(accessPoint.getSecurityProtocol())
                .available(accessPoint.getAvailable())
                .enabled(accessPoint.getEnabled())
                .type(accessPoint.getType())
                .terminalId(terminalId)
                .build();

        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<AccessPointDTO> createAccessPoint(@RequestBody AccessPointCreateRequest request) {
        // Vérifier si un access point existe déjà pour ce terminal
        if (accessPointRepository.findByTerminalId(request.getTerminalId()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        AccessPoint accessPoint = AccessPoint.builder()
                .ssid(request.getSsid())
                .password(request.getPassword())
                .securityProtocol(request.getSecurityProtocol())
                .available(request.getAvailable() != null ? request.getAvailable() : false)
                .enabled(request.getEnabled() != null ? request.getEnabled() : false)
                .type(request.getType())
                .terminal(Terminal.builder().id(request.getTerminalId()).build())
                .build();

        AccessPoint saved = accessPointRepository.save(accessPoint);

        AccessPointDTO dto = AccessPointDTO.builder()
                .id(saved.getId())
                .ssid(saved.getSsid())
                .password(saved.getPassword())
                .securityProtocol(saved.getSecurityProtocol())
                .available(saved.getAvailable())
                .enabled(saved.getEnabled())
                .type(saved.getType())
                .terminalId(request.getTerminalId())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{terminalId}")
    public ResponseEntity<AccessPointDTO> updateAccessPoint(
            @PathVariable Long terminalId,
            @RequestBody AccessPointUpdateRequest request) {

        AccessPoint accessPoint = accessPointRepository
                .findByTerminalId(terminalId)
                .orElseThrow(() -> new RuntimeException("Access point not found for terminal: " + terminalId));

        if (request.getSsid() != null) {
            accessPoint.setSsid(request.getSsid());
        }
        if (request.getPassword() != null) {
            accessPoint.setPassword(request.getPassword());
        }
        if (request.getSecurityProtocol() != null) {
            accessPoint.setSecurityProtocol(request.getSecurityProtocol());
        }
        if (request.getAvailable() != null) {
            accessPoint.setAvailable(request.getAvailable());
        }
        if (request.getEnabled() != null) {
            accessPoint.setEnabled(request.getEnabled());
        }
        if (request.getType() != null) {
            accessPoint.setType(request.getType());
        }

        AccessPoint updated = accessPointRepository.save(accessPoint);

        AccessPointDTO dto = AccessPointDTO.builder()
                .id(updated.getId())
                .ssid(updated.getSsid())
                .password(updated.getPassword())
                .securityProtocol(updated.getSecurityProtocol())
                .available(updated.getAvailable())
                .enabled(updated.getEnabled())
                .type(updated.getType())
                .terminalId(terminalId)
                .build();

        return ResponseEntity.ok(dto);
    }

//    @GetMapping("/{terminalId}/qr-code")
//    public ResponseEntity<byte[]> getWifiQrCode(
//            @PathVariable Long terminalId,
//            @RequestParam(defaultValue = "300") int size) {
//
//        try {
//            AccessPoint accessPoint = accessPointRepository
//                    .findByTerminalId(terminalId)
//                    .orElseThrow(() -> new RuntimeException("Access point not found for terminal: " + terminalId));
//
//            byte[] qrCode = qrCodeService.generateQrCode(accessPoint, size, size);
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.IMAGE_PNG);
//            headers.setContentLength(qrCode.length);
//            headers.set("Cache-Control", "max-age=3600");
//
//            return new ResponseEntity<>(qrCode, headers, HttpStatus.OK);
//
//        } catch (WriterException | IOException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
@GetMapping("/{terminalId}/qr-code")
public ResponseEntity<byte[]> getWifiQrCode(
        @PathVariable Long terminalId,
        @RequestParam(defaultValue = "300") int size,
        @RequestParam(required = false) String foreground,
        @RequestParam(required = false) String background) {

    try {
        AccessPoint accessPoint = accessPointRepository
                .findByTerminalId(terminalId)
                .orElseThrow(() -> new RuntimeException("Access point not found for terminal: " + terminalId));

        byte[] qrCode;

        if (foreground != null && background != null) {
            qrCode = qrCodeService.generateQrCode(accessPoint, size, size, foreground, background);
        } else if (foreground != null) {
            qrCode = qrCodeService.generateQrCode(accessPoint, size, size, foreground, "FFFFFF");
        } else if (background != null) {
            qrCode = qrCodeService.generateQrCode(accessPoint, size, size, "000000", background);
        } else {
            qrCode = qrCodeService.generateQrCode(accessPoint, size, size);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(qrCode.length);
        headers.set("Cache-Control", "max-age=3600");

        return new ResponseEntity<>(qrCode, headers, HttpStatus.OK);

    } catch (WriterException | IOException e) {
        log.error("Error generating QR code", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } catch (IllegalArgumentException e) {
        log.error("Invalid color format", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}

    @DeleteMapping("/{terminalId}")
    public ResponseEntity<Void> deleteAccessPoint(@PathVariable Long terminalId) {
        AccessPoint accessPoint = accessPointRepository
                .findByTerminalId(terminalId)
                .orElseThrow(() -> new RuntimeException("Access point not found for terminal: " + terminalId));

        accessPointRepository.delete(accessPoint);
        return ResponseEntity.noContent().build();
    }
}