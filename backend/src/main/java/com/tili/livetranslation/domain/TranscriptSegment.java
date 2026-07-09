package com.tili.livetranslation.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "transcript_segment")
public class TranscriptSegment extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(name = "session_id", nullable = false)
    public UUID sessionId;

    @Column(name = "sequence_no", nullable = false)
    public long sequenceNo;

    @Column(name = "source_language", nullable = false)
    public String sourceLanguage;

    @Column(name = "original_text", nullable = false, columnDefinition = "TEXT")
    public String originalText;

    @Column(name = "is_final", nullable = false)
    public boolean isFinal = false;

    public Float confidence;

    @Column(name = "start_offset_ms")
    public Long startOffsetMs;

    @Column(name = "end_offset_ms")
    public Long endOffsetMs;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt = Instant.now();

    public static List<TranscriptSegment> findBySessionOrdered(UUID sessionId) {
        return list("sessionId = ?1 order by sequenceNo asc", sessionId);
    }

    public static TranscriptSegment findBySessionAndSequence(UUID sessionId, long sequenceNo) {
        return find("sessionId = ?1 and sequenceNo = ?2", sessionId, sequenceNo).firstResult();
    }
}
