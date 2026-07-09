package com.tili.livetranslation.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "provider_config")
public class ProviderConfig extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(name = "provider_code", nullable = false)
    public String providerCode; // e.g. OPENAI_REALTIME, AZURE_SPEECH

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false)
    public ProviderType providerType;

    @Column(nullable = false)
    public int priority = 100;

    @Column(nullable = false)
    public boolean enabled = true;

    @Column(name = "credentials_ref")
    public String credentialsRef; // alias into secret store, never the raw key

    @Column(name = "supported_languages")
    public String supportedLanguages; // comma separated

    @Column(name = "supported_voices")
    public String supportedVoices; // comma separated

    @Column(name = "cost_limit_cents")
    public Long costLimitCents;

    @Column(name = "timeout_ms", nullable = false)
    public int timeoutMs = 8000;

    @Column(name = "fallback_provider_code")
    public String fallbackProviderCode;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt = Instant.now();

    public static List<ProviderConfig> findEnabledByType(ProviderType type) {
        return list("providerType = ?1 and enabled = true order by priority asc", type);
    }
}
