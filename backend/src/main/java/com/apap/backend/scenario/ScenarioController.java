package com.apap.backend.scenario;

import com.apap.backend.common.ApiResponse;
import com.apap.backend.user.User;
import com.apap.backend.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/scenarios")
public class ScenarioController {

    private final ScenarioRepository scenarioRepository;
    private final UserRepository userRepository;

    public ScenarioController(ScenarioRepository scenarioRepository, UserRepository userRepository) {
        this.scenarioRepository = scenarioRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ApiResponse<ScenarioResponse> create(@Valid @RequestBody ScenarioRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        Scenario scenario = new Scenario(
                user,
                request.name(),
                request.targetLocation(),
                request.behaviorText(),
                request.conditionsJson(),
                request.threshold()
        );
        return ApiResponse.ok(ScenarioResponse.from(scenarioRepository.save(scenario)));
    }

    @GetMapping
    public ApiResponse<List<ScenarioResponse>> list(@RequestParam Long userId) {
        List<ScenarioResponse> scenarios = scenarioRepository.findAllByUserIdOrderByIdDesc(userId)
                .stream()
                .map(ScenarioResponse::from)
                .toList();
        return ApiResponse.ok(scenarios);
    }

    @PutMapping("/{scenarioId}")
    public ApiResponse<ScenarioResponse> update(
            @PathVariable Long scenarioId,
            @Valid @RequestBody ScenarioRequest request
    ) {
        Scenario scenario = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new EntityNotFoundException("시나리오를 찾을 수 없습니다."));
        scenario.update(
                request.name(),
                request.targetLocation(),
                request.behaviorText(),
                request.conditionsJson(),
                request.threshold(),
                request.active()
        );
        return ApiResponse.ok(ScenarioResponse.from(scenarioRepository.save(scenario)));
    }

    public record ScenarioRequest(
            Long userId,
            @NotBlank String name,
            String targetLocation,
            @NotBlank String behaviorText,
            String conditionsJson,
            double threshold,
            boolean active
    ) {
    }

    public record ScenarioResponse(
            Long id,
            Long userId,
            String name,
            String targetLocation,
            String behaviorText,
            String conditionsJson,
            double threshold,
            boolean active
    ) {
        static ScenarioResponse from(Scenario scenario) {
            return new ScenarioResponse(
                    scenario.getId(),
                    scenario.getUser().getId(),
                    scenario.getName(),
                    scenario.getTargetLocation(),
                    scenario.getBehaviorText(),
                    scenario.getConditionsJson(),
                    scenario.getThreshold(),
                    scenario.isActive()
            );
        }
    }
}
