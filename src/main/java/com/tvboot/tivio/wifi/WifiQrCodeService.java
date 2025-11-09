package com.tvboot.tivio.wifi;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

@Slf4j
@Service
public class WifiQrCodeService {

    // Couleurs par défaut
    private static final int DEFAULT_FOREGROUND = 0xFF000000; // Noir
    private static final int DEFAULT_BACKGROUND = 0xFFFFFFFF; // Blanc

    public byte[] generateQrCode(AccessPoint accessPoint, int width, int height)
            throws WriterException, IOException {
        return generateQrCode(accessPoint, width, height, DEFAULT_FOREGROUND, DEFAULT_BACKGROUND);
    }

    public byte[] generateQrCode(AccessPoint accessPoint, int width, int height,
                                 String foregroundHex, String backgroundHex)
            throws WriterException, IOException {
        int foreground = hexToArgb(foregroundHex);
        int background = hexToArgb(backgroundHex);
        return generateQrCode(accessPoint, width, height, foreground, background);
    }

    public byte[] generateQrCode(AccessPoint accessPoint, int width, int height,
                                 int foregroundColor, int backgroundColor)
            throws WriterException, IOException {

        String wifiString = accessPoint.toWifiQrString();

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(
                wifiString,
                BarcodeFormat.QR_CODE,
                width,
                height,
                hints
        );

        BufferedImage qrImage = matrixToColoredImage(bitMatrix, foregroundColor, backgroundColor);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrImage, "PNG", baos);

        return baos.toByteArray();
    }

    public BufferedImage generateQrCodeImage(AccessPoint accessPoint, int size,
                                             int foregroundColor, int backgroundColor)
            throws WriterException {

        String wifiString = accessPoint.toWifiQrString();

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(
                wifiString,
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
        );

        return matrixToColoredImage(bitMatrix, foregroundColor, backgroundColor);
    }

    private BufferedImage matrixToColoredImage(BitMatrix matrix,
                                               int foregroundColor,
                                               int backgroundColor) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? foregroundColor : backgroundColor);
            }
        }

        return image;
    }

    private int hexToArgb(String hex) {
        // Supporte les formats: #RGB, #RRGGBB, #AARRGGBB, RGB, RRGGBB, AARRGGBB
        hex = hex.replace("#", "");

        switch (hex.length()) {
            case 3: // RGB -> RRGGBB
                hex = String.format("%c%c%c%c%c%c",
                        hex.charAt(0), hex.charAt(0),
                        hex.charAt(1), hex.charAt(1),
                        hex.charAt(2), hex.charAt(2));
                return (int) Long.parseLong("FF" + hex, 16);

            case 6: // RRGGBB -> AARRGGBB
                return (int) Long.parseLong("FF" + hex, 16);

            case 8: // AARRGGBB
                return (int) Long.parseLong(hex, 16);

            default:
                throw new IllegalArgumentException("Invalid hex color: " + hex);
        }
    }

    public int rgbToArgb(int red, int green, int blue) {
        return (0xFF << 24) | (red << 16) | (green << 8) | blue;
    }

    public int rgbaToArgb(int red, int green, int blue, int alpha) {
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
}