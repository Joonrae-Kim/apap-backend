package com.apap.backend.event;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetectionEventRepository extends JpaRepository<DetectionEvent, Long> {
    List<DetectionEvent> findAllByScenarioUserIdOrderByDetectedAtDesc(Long userId);

    long countByScenarioUserIdAndEventType(Long userId, DetectionEventType eventType);

    long countByScenarioUserIdAndEventTypeIn(Long userId, java.util.Collection<DetectionEventType> eventTypes);
}
