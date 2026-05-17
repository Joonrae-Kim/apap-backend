package com.apap.backend.dashboard;

import com.apap.backend.alert.AlertRepository;
import com.apap.backend.alert.AlertStatus;
import com.apap.backend.analysis.AnalysisJobRepository;
import com.apap.backend.common.ApiResponse;
import com.apap.backend.event.DetectionEventRepository;
import com.apap.backend.event.DetectionEventType;
import com.apap.backend.scenario.ScenarioRepository;
import com.apap.backend.video.VideoSourceRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final ScenarioRepository scenarioRepository;
    private final VideoSourceRepository videoSourceRepository;
    private final AnalysisJobRepository analysisJobRepository;
    private final DetectionEventRepository detectionEventRepository;
    private final AlertRepository alertRepository;

    public DashboardController(
            ScenarioRepository scenarioRepository,
            VideoSourceRepository videoSourceRepository,
            AnalysisJobRepository analysisJobRepository,
            DetectionEventRepository detectionEventRepository,
            AlertRepository alertRepository
    ) {
        this.scenarioRepository = scenarioRepository;
        this.videoSourceRepository = videoSourceRepository;
        this.analysisJobRepository = analysisJobRepository;
        this.detectionEventRepository = detectionEventRepository;
        this.alertRepository = alertRepository;
    }

    @GetMapping("/summary")
    public ApiResponse<DashboardSummary> summary(@RequestParam Long userId) {
        return ApiResponse.ok(new DashboardSummary(
                scenarioRepository.findAllByUserIdOrderByIdDesc(userId).size(),
                videoSourceRepository.findAllByUserIdOrderByIdDesc(userId).size(),
                analysisJobRepository.findAllByScenarioUserIdOrderByIdDesc(userId).size(),
                detectionEventRepository.countByScenarioUserIdAndEventType(userId, DetectionEventType.ABNORMAL),
                alertRepository.countByReceiverIdAndStatusNot(userId, AlertStatus.READ)
        ));
    }

    public record DashboardSummary(
            long scenarios,
            long videos,
            long analysisJobs,
            long abnormalEvents,
            long unreadAlerts
    ) {
    }
}
