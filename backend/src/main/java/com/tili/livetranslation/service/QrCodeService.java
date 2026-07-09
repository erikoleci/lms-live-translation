package com.tili.livetranslation.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@ApplicationScoped
public class QrCodeService {

    @ConfigProperty(name = "zana.public-base-url", defaultValue = "https://zana.tili.local")
    String publicBaseUrl;

    public String buildJoinUrl(String joinCode) {
        return publicBaseUrl + "/join/" + joinCode;
    }

    /** Renders a PNG QR code encoding the public join URL. */
    public byte[] generateQrPng(String joinCode) {
        try {
            String url = buildJoinUrl(joinCode);
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE, 320, 320);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }
}
