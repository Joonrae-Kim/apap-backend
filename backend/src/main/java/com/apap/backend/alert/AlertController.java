package com.apap.backend.alert;

import com.apap.backend.common.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertRepository alertRepository;

    public AlertController(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @GetMapping
    public ApiResponse<List<AlertResponse>> list(@RequestParam Long userId) {
        List<AlertResponse> alerts = alertRepository.findAllByReceiverIdOrderByIdDesc(userId)
                .stream()
                .map(AlertResponse::from)
                .toList();
        return ApiResponse.ok(alerts);
    }

    @PatchMapping("/{alertId}/read")
    public ApiResponse<AlertResponse> read(@PathVariable Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new EntityNotFoundException("알림을 찾을 수 없습니다."));
        alert.markAsRead();
        return ApiResponse.ok(AlertResponse.from(alertRepository.save(alert)));
    }

    public record AlertResponse(
            Long id,
            Long detectionEventId,
            Long receiverId,
            String channel,
            AlertStatus status,
            String message,
            LocalDateTime sentAt,
            LocalDateTime readAt
    ) {
        static AlertResponse from(Alert alert) {
            return new AlertResponse(
                    alert.getId(),
                    alert.getDetectionEvent().getId(),
                    alert.getReceiver().getId(),
                    alert.getChannel(),
                    alert.getStatus(),
                    alert.getMessage(),
                    alert.getSentAt(),
                    alert.getReadAt()
            );
        }
    }
}
