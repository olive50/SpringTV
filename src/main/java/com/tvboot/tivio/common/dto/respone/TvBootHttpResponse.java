package com.tvboot.tivio.common.dto.respone;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Data
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TvBootHttpResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    protected LocalDateTime timeStamp;

    protected int statusCode;
    protected HttpStatus status;
    protected String reason;
    protected String message;
    protected String developerMessage;
    protected Map<String, Object> data;

    // Static factory methods that return BUILDER
    public static TvBootHttpResponseBuilder<?, ?> success() {
        return TvBootHttpResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .reason(HttpStatus.OK.getReasonPhrase())
                .timeStamp(LocalDateTime.now());
    }

    public static TvBootHttpResponseBuilder<?, ?> created() {
        return TvBootHttpResponse.builder()
                .statusCode(HttpStatus.CREATED.value())
                .status(HttpStatus.CREATED)
                .reason(HttpStatus.CREATED.getReasonPhrase())
                .timeStamp(LocalDateTime.now());
    }

    public static TvBootHttpResponseBuilder<?, ?> error(HttpStatus status, String message) {
        return TvBootHttpResponse.builder()
                .statusCode(status.value())
                .status(status)
                .reason(status.getReasonPhrase())
                .message(message)
                .timeStamp(LocalDateTime.now());
    }

    public static TvBootHttpResponseBuilder<?, ?> badRequest(String message) {
        return error(HttpStatus.BAD_REQUEST, message);
    }

    public static TvBootHttpResponseBuilder<?, ?> notFound(String message) {
        return error(HttpStatus.NOT_FOUND, message);
    }

    public static TvBootHttpResponseBuilder<?, ?> unauthorized(String message) {
        return error(HttpStatus.UNAUTHORIZED, message);
    }

    public static TvBootHttpResponseBuilder<?, ?> forbidden(String message) {
        return error(HttpStatus.FORBIDDEN, message);
    }

    public static TvBootHttpResponseBuilder<?, ?> internalServerError(String message) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    // IPTV-specific status methods that return BUILDER
    public static TvBootHttpResponseBuilder<?, ?> streamUnavailable(String message) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, message);
    }

    public static TvBootHttpResponseBuilder<?, ?> channelNotFound(String channelId) {
        return error(HttpStatus.NOT_FOUND, "Channel not found: " + channelId);
    }

    public static TvBootHttpResponseBuilder<?, ?> deviceNotAuthorized(String deviceId) {
        return error(HttpStatus.FORBIDDEN, "Device not authorized: " + deviceId);
    }

    // Instance methods for adding data (work on the built object)
    public TvBootHttpResponse addData(String key, Object value) {
        if (this.data == null) {
            this.data = new HashMap<>();
        }
        this.data.put(key, value);
        return this;
    }

    public TvBootHttpResponse addChannel(Object channel) {
        return addData("channel", channel);
    }

    public TvBootHttpResponse addChannels(List<?> channels) {
        return addData("channels", channels);
    }

    public TvBootHttpResponse addStream(Object stream) {
        return addData("stream", stream);
    }

    public TvBootHttpResponse addStreams(List<?> streams) {
        return addData("streams", streams);
    }

    public TvBootHttpResponse addHotel(Object hotel) {
        return addData("hotel", hotel);
    }

    public TvBootHttpResponse addRoom(Object room) {
        return addData("room", room);
    }

    public TvBootHttpResponse addDevice(Object device) {
        return addData("device", device);
    }

    public TvBootHttpResponse addUser(Object user) {
        return addData("user", user);
    }

    public TvBootHttpResponse addCount(long count) {
        return addData("count", count);
    }

    public TvBootHttpResponse addEPG(Object epg) {
        return addData("epg", epg);
    }

    public TvBootHttpResponse addPlaylist(Object playlist) {
        return addData("playlist", playlist);
    }

    public TvBootHttpResponse addPagination(int page, int size, long total) {
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", page);
        pagination.put("size", size);
        pagination.put("total", total);
        pagination.put("totalPages", (int) Math.ceil((double) total / size));
        pagination.put("hasNext", page < (int) Math.ceil((double) total / size) - 1);
        pagination.put("hasPrevious", page > 0);
        return addData("pagination", pagination);
    }

    // Static factory methods that return ResponseEntity directly
    public static ResponseEntity<TvBootHttpResponse> ok(String message) {
        TvBootHttpResponse response = TvBootHttpResponse.success()
                .message(message)
                .build();
        return ResponseEntity.ok(response);
    }

    public static ResponseEntity<TvBootHttpResponse> okWithChannels(String message, List<?> channels) {
        TvBootHttpResponse response = TvBootHttpResponse.success()
                .message(message)
                .build()
                .addChannels(channels);
        return ResponseEntity.ok(response);
    }

    public static ResponseEntity<TvBootHttpResponse> okWithChannels(String message, List<?> channels, int page, int size, long total) {
        TvBootHttpResponse response = TvBootHttpResponse.success()
                .message(message)
                .build()
                .addChannels(channels)
                .addPagination(page, size, total);
        return ResponseEntity.ok(response);
    }

    public static ResponseEntity<TvBootHttpResponse> createdWithChannel(String message, Object channel) {
        TvBootHttpResponse response = TvBootHttpResponse.created()
                .message(message)
                .build()
                .addChannel(channel);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    public static ResponseEntity<TvBootHttpResponse> badRequestResponse(String message) {
        TvBootHttpResponse response = TvBootHttpResponse.badRequest(message).build();
        return ResponseEntity.badRequest().body(response);
    }

    public static ResponseEntity<TvBootHttpResponse> notFoundResponse(String message) {
        TvBootHttpResponse response = TvBootHttpResponse.notFound(message).build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    public static ResponseEntity<TvBootHttpResponse> channelNotFoundResponse(String channelId) {
        TvBootHttpResponse response = TvBootHttpResponse.channelNotFound(channelId).build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    public static ResponseEntity<TvBootHttpResponse> streamUnavailableResponse(String message, String streamUrl) {
        TvBootHttpResponse response = TvBootHttpResponse.streamUnavailable(message)
                .build()
                .addData("streamUrl", streamUrl);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    public static ResponseEntity<TvBootHttpResponse> deviceNotAuthorizedResponse(String deviceId) {
        TvBootHttpResponse response = TvBootHttpResponse.deviceNotAuthorized(deviceId).build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    public static ResponseEntity<TvBootHttpResponse> internalServerErrorResponse(String message, String developerMessage) {
        TvBootHttpResponse response = TvBootHttpResponse.internalServerError(message)
                .developerMessage(developerMessage)
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // Validation error handling
    public static ResponseEntity<TvBootHttpResponse> validationErrorResponse(String message, List<String> errors) {
        TvBootHttpResponse response = TvBootHttpResponse.badRequest(message).build();
        response.addData("validationErrors", errors);
        return ResponseEntity.badRequest().body(response);
    }
}