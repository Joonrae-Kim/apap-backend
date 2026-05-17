package com.apap.backend.scenario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScenarioRepository extends JpaRepository<Scenario, Long> {
    List<Scenario> findAllByUserIdOrderByIdDesc(Long userId);
}
