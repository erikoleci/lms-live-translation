package com.tili.livetranslation.service;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/** Spec 4.1: "Expire inactive sessions automatically." */
@ApplicationScoped
public class SessionExpiryScheduler {

    @Inject
    SessionService sessionService;

    @ConfigProperty(name = "zana.session.default-expiry-minutes-inactive")
    int inactivityMinutes;

    @Scheduled(every = "5m")
    void expireStaleSessions() {
        sessionService.expireInactiveSessions(inactivityMinutes);
    }
}
