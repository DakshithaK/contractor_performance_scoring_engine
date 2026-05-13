package com.contractoriq.kafka;

import java.util.UUID;

public class ScoreRecalculationEvent {

    private UUID contractorId;
    private String trigger;

    public ScoreRecalculationEvent() {}

    public ScoreRecalculationEvent(UUID contractorId, String trigger) {
        this.contractorId = contractorId;
        this.trigger = trigger;
    }

    public UUID getContractorId() { return contractorId; }
    public void setContractorId(UUID contractorId) { this.contractorId = contractorId; }

    public String getTrigger() { return trigger; }
    public void setTrigger(String trigger) { this.trigger = trigger; }
}
