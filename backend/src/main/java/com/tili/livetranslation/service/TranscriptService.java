package com.tili.livetranslation.service;

import com.tili.livetranslation.domain.LiveSession;
import com.tili.livetranslation.domain.TranscriptSegment;
import com.tili.livetranslation.exception.ForbiddenException;
import com.tili.livetranslation.websocket.event.EventType;
import com.tili.livetranslation.websocket.event.WsEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Owns the transcript_segment table and the partial->final replacement rule
 * from spec section 9: "final segments replace previous partial segments,
 * every segment must include sequence number and timestamp".
 */
@ApplicationScoped
public class TranscriptService {

    @Inject
    SessionBroadcastService broadcastService;

    @Inject
    TranslationOrchestrator translationOrchestrator;

    // one monotonic counter per active session; reset when the session ends.
    private final Map<UUID, AtomicLong> sequenceCounters = new ConcurrentHashMap<>();

    @Transactional
    public TranscriptSegment recordSegment(UUID sessionId, String sourceLanguage, String text,
                                            boolean isFinal, Float confidence,
                                            Long startOffsetMs, Long endOffsetMs) {
        long seq = sequenceCounters
                .computeIfAbsent(sessionId, id -> new AtomicLong(0))
                .incrementAndGet();

        TranscriptSegment segment = new TranscriptSegment();
        segment.sessionId = sessionId;
        segment.sequenceNo = seq;
        segment.sourceLanguage = sourceLanguage;
        segment.originalText = text;
        segment.isFinal = isFinal;
        segment.confidence = confidence;
        segment.startOffsetMs = startOffsetMs;
        segment.endOffsetMs = endOffsetMs;
        segment.persist();

        broadcastService.broadcastToSession(sessionId, new WsEvent(isFinal ? EventType.TRANSCRIPT_FINAL : EventType.TRANSCRIPT_PARTIAL,
                Map.of(
                        "segmentId", segment.id.toString(),
                        "sequenceNo", seq,
                        "text", text,
                        "isFinal", isFinal
                )));

        // Kick off translation for every enabled target language.
        // Partial segments are debounced, final segments are always (re-)translated (spec 9).
        LiveSession session = LiveSession.findById(sessionId);
        if (session != null) {
            for (String targetLang : session.targetLanguageList()) {
                if (targetLang.equalsIgnoreCase(sourceLanguage)) continue; // no self-translation
                translationOrchestrator.translateSegment(segment, targetLang, isFinal);
            }
        }

        return segment;
    }

    @Transactional
    public List<TranscriptSegment> listForSession(UUID sessionId, boolean requesterAllowed) {
        if (!requesterAllowed) {
            throw new ForbiddenException("Transcript access is not enabled for this session.");
        }
        return TranscriptSegment.findBySessionOrdered(sessionId);
    }

    public void resetSequenceCounter(UUID sessionId) {
        sequenceCounters.remove(sessionId);
    }
}
