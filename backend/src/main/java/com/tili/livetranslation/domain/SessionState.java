package com.tili.livetranslation.domain;

/**
 * Session lifecycle states (spec section 4.1).
 * Transitions are enforced in SessionService, not in the entity itself.
 */
public enum SessionState {
    CREATED,
    WAITING,
    ACTIVE,
    PAUSED,
    ENDED,
    FAILED,
    EXPIRED;

    /** Valid forward transitions, keyed by current state. */
    public boolean canTransitionTo(SessionState target) {
        return switch (this) {
            case CREATED -> target == WAITING || target == ACTIVE || target == FAILED || target == EXPIRED;
            case WAITING -> target == ACTIVE || target == FAILED || target == EXPIRED;
            case ACTIVE -> target == PAUSED || target == ENDED || target == FAILED;
            case PAUSED -> target == ACTIVE || target == ENDED || target == FAILED;
            case ENDED, FAILED, EXPIRED -> false; // terminal states
        };
    }
}
