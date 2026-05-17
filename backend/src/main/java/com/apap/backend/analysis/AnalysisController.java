package com.apap.backend.analysis;

import com.apap.backend.alert.Alert;
import com.apap.backend.alert.AlertRepository;
import com.apap.backend.common.ApiResponse;
import com.apap.backend.event.DetectionEvent;
import com.apap.backend.event.DetectionEventRepository;
import com.apap.backend.event.DetectionEventType;
import com.apap.backend.event.Severity;
import com.apap.backend.scenario.Scenario;
import com.apap.backend.scenario.ScenarioRepository;
import com.apap.backend.video.VideoSource;
import com.apap.backend.video.VideoSourceRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private static final Set<DetectionEventType> ABNORMAL_TYPES =
            Set.of(DetectionEventType.FALL, DetectionEventType.INTRUSION, DetectionEventType.ANOMALOUS);

    private final AnalysisJobRepository analysisJobRepository;
    private final ScenarioRepository scenarioRepository;
    private final VideoSourceRepository videoSourceRepository;
    private final DetectionEventRepository detectionEventRepository;
    private final AlertRepository alertRepository;
    private final String aiServerUrl;
    private final String baseUrl;
    private final RestClient restClient;

    public AnalysisController(
            AnalysisJobRepository analysisJobRepository,
            ScenarioRepository scenarioRepository,
            VideoSourceRepository videoSourceRepository,
            DetectionEventRepository detectionEventRepository,
            AlertRepository alertRepository,
            @Value("${apap.ai-server-url}") String aiServerUrl,
            @Value("${apap.base-url}") String baseUrl
    ) {
        this.analysisJobRepository = analysisJobRepository;
        this.scenarioRepository = scenarioRepository;
        this.videoSourceRepository = videoSourceRepository;
        this.detectionEventRepository = detectionEventRepository;
        this.alertRepository = alertRepository;
        this.aiServerUrl = aiServerUrl;
        this.baseUrl = baseUrl;
        this.restClient = RestClient.create();
    }

    @PostMapping("/jobs")
    public ApiResponse<AnalysisJobResponse> createJob(@Valid @RequestBody AnalysisJobRequest request) {
        Scenario scenario = scenarioRepository.findById(request.scenarioId())
                .orElseThrow(() -> new EntityNotFoundException("시나리오를 찾을 수 없습니다."));
        VideoSource videoSource = videoSourceRepository.findById(request.videoSourceId())
                .orElseThrow(() -> new EntityNotFoundException("영상 소스를 찾을 수 없습니다."));

        AnalysisJob job = analysisJobRepository.save(new AnalysisJob(scenario, videoSource));

        Map<String, Object> aiRequestPayload = Map.of(
                "job_id", job.getId(),
                "scenario", Map.of(
                        "id", scenario.getId(),
                        "name", scenario.getName(),
                        "behavior_text", scenario.getBehaviorText(),
                        "threshold", scenario.getThreshold()
                ),
                "video", Map.of(
                        "id", videoSource.getId(),
                        "source_url", videoSource.getSourceUrl()
                ),
                "callback_url", baseUrl + "/api/analysis/callback"
        );

        try {
            restClient.post()
                    .uri(aiServerUrl + "/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(aiRequestPayload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            job.complete(AnalysisJobStatus.FAILED, "AI 서버 호출 실패: " + e.getMessage());
            analysisJobRepository.save(job);
        }

        return ApiResponse.ok(AnalysisJobResponse.from(job), "분석 요청이 접수되었습니다.");
    }

    @GetMapping("/jobs")
    public ApiResponse<List<AnalysisJobResponse>> listJobs(@RequestParam Long userId) {
        List<AnalysisJobResponse> jobs = analysisJobRepository.findAllByScenarioUserIdOrderByIdDesc(userId)
                .stream()
                .map(AnalysisJobResponse::from)
                .toList();
        return ApiResponse.ok(jobs);
    }

    @GetMapping("/jobs/{jobId}")
    public ApiResponse<AnalysisJobResponse> getJob(@PathVariable Long jobId) {
        AnalysisJob job = analysisJobRepository.findById(jobId)
                .orElseThrow(() -> new EntityNotFoundException("분석 작업을 찾을 수 없습니다."));
        return ApiResponse.ok(AnalysisJobResponse.from(job));
    }

    @PostMapping("/callback")
    public ApiResponse<Void> callback(@Valid @RequestBody AnalysisCallbackRequest request) {
        AnalysisJob job = analysisJobRepository.findById(request.jobId())
                .orElseThrow(() -> new EntityNotFoundException("분석 작업을 찾을 수 없습니다."));
        job.complete(request.status(), request.errorMessage());
        analysisJobRepository.save(job);

        for (DetectionEventRequest eventRequest : request.events()) {
            DetectionEvent event = detectionEventRepository.save(new DetectionEvent(
                    job,
                    eventRequest.eventType(),
                    eventRequest.severity(),
                    eventRequest.confidenceScore(),
                    eventRequest.detectedAt(),
                    eventRequest.snapshotUrl(),
                    eventRequest.clipUrl(),
                    eventRequest.resultJson()
            ));

            if (ABNORMAL_TYPES.contains(eventRequest.eventType())) {
                String message = switch (eventRequest.eventType()) {
                    case FALL -> "낙상이 감지되었습니다. severity=" + eventRequest.severity();
                    case INTRUSION -> "침입이 감지되었습니다. severity=" + eventRequest.severity();
                    case ANOMALOUS -> "이상행동이 감지되었습니다. severity=" + eventRequest.severity();
                    default -> "비정상 행동이 감지되었습니다. severity=" + eventRequest.severity();
                };
                alertRepository.save(new Alert(event, job.getScenario().getUser(), message));
            }
        }

        return ApiResponse.ok(null, "AI 분석 결과가 저장되었습니다.");
    }

    public record AnalysisJobRequest(Long scenarioId, Long videoSourceId) {
    }

    public record AnalysisJobResponse(
            Long id,
            Long scenarioId,
            Long videoSourceId,
            AnalysisJobStatus status,
            LocalDateTime requestedAt,
            LocalDateTime completedAt,
            String errorMessage
    ) {
        static AnalysisJobResponse from(AnalysisJob job) {
            return new AnalysisJobResponse(
                    job.getId(),
                    job.getScenario().getId(),
                    job.getVideoSource().getId(),
                    job.getStatus(),
                    job.getRequestedAt(),
                    job.getCompletedAt(),
                    job.getErrorMessage()
            );
        }
    }

    public record AnalysisCallbackRequest(
            Long jobId,
            AnalysisJobStatus status,
            String errorMessage,
            List<DetectionEventRequest> events
    ) {
    }

    public record DetectionEventRequest(
            DetectionEventType eventType,
            Severity severity,
            double confidenceScore,
            LocalDateTime detectedAt,
            String snapshotUrl,
            String clipUrl,
            String resultJson
    ) {
    }
}
