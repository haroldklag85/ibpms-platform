package com.ibpms.poc.infrastructure.mq;

public enum MessagePriority {
    P1(1, "Crítico", 1),
    P2(2, "Normal", 10),
    P3(3, "Batch", 50);

    private final int level;
    private final String description;
    private final int recommendedPrefetch;

    MessagePriority(int level, String description, int recommendedPrefetch) {
        this.level = level;
        this.description = description;
        this.recommendedPrefetch = recommendedPrefetch;
    }

    public int getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }

    public int getRecommendedPrefetch() {
        return recommendedPrefetch;
    }
}
