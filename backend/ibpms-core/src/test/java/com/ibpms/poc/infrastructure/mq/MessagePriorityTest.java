package com.ibpms.poc.infrastructure.mq;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MessagePriorityTest {

    @Test
    void testMessagePriorityEnumValues() {
        assertThat(MessagePriority.values()).hasSize(3);

        assertThat(MessagePriority.P1.getLevel()).isEqualTo(1);
        assertThat(MessagePriority.P1.getDescription()).isEqualTo("Crítico");
        assertThat(MessagePriority.P1.getRecommendedPrefetch()).isEqualTo(1);

        assertThat(MessagePriority.P2.getLevel()).isEqualTo(2);
        assertThat(MessagePriority.P2.getDescription()).isEqualTo("Normal");
        assertThat(MessagePriority.P2.getRecommendedPrefetch()).isEqualTo(10);

        assertThat(MessagePriority.P3.getLevel()).isEqualTo(3);
        assertThat(MessagePriority.P3.getDescription()).isEqualTo("Batch");
        assertThat(MessagePriority.P3.getRecommendedPrefetch()).isEqualTo(50);
    }
}
