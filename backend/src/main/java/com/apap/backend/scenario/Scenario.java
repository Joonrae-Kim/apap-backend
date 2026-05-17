package com.apap.backend.scenario;

import com.apap.backend.common.BaseEntity;
import com.apap.backend.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "scenarios")
public class Scenario extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 150)
    private String targetLocation;

    @Lob
    @Column(nullable = false)
    private String behaviorText;

    @Lob
    private String conditionsJson;

    @Column(nullable = false)
    private double threshold;

    @Column(nullable = false)
    private boolean active;

    protected Scenario() {
    }

    public Scenario(User user, String name, String targetLocation, String behaviorText, String conditionsJson, double threshold) {
        this.user = user;
        this.name = name;
        this.targetLocation = targetLocation;
        this.behaviorText = behaviorText;
        this.conditionsJson = conditionsJson;
        this.threshold = threshold;
        this.active = true;
    }

    public void update(String name, String targetLocation, String behaviorText, String conditionsJson, double threshold, boolean active) {
        this.name = name;
        this.targetLocation = targetLocation;
        this.behaviorText = behaviorText;
        this.conditionsJson = conditionsJson;
        this.threshold = threshold;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    public String getTargetLocation() {
        return targetLocation;
    }

    public String getBehaviorText() {
        return behaviorText;
    }

    public String getConditionsJson() {
        return conditionsJson;
    }

    public double getThreshold() {
        return threshold;
    }

    public boolean isActive() {
        return active;
    }
}
