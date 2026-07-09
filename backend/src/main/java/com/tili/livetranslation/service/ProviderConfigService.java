package com.tili.livetranslation.service;

import com.tili.livetranslation.domain.ProviderConfig;
import com.tili.livetranslation.domain.ProviderType;
import com.tili.livetranslation.dto.ProviderConfigRequest;
import com.tili.livetranslation.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Admin-facing CRUD for STT/Translation/TTS provider configuration
 * (spec section 12: "Admin can configure ... provider priority ...
 * without code change", acceptance criteria section 15).
 */
@ApplicationScoped
public class ProviderConfigService {

    @Transactional
    public ProviderConfig create(ProviderConfigRequest req) {
        ProviderConfig c = new ProviderConfig();
        apply(c, req);
        c.persist();
        return c;
    }

    public List<ProviderConfig> listAll() {
        return ProviderConfig.listAll();
    }

    public ProviderConfig getOrThrow(UUID id) {
        ProviderConfig c = ProviderConfig.findById(id);
        if (c == null) throw new NotFoundException("Provider config not found: " + id);
        return c;
    }

    @Transactional
    public ProviderConfig update(UUID id, ProviderConfigRequest req) {
        ProviderConfig c = getOrThrow(id);
        apply(c, req);
        c.updatedAt = Instant.now();
        return c;
    }

    @Transactional
    public void disable(UUID id) {
        ProviderConfig c = getOrThrow(id);
        c.enabled = false;
        c.updatedAt = Instant.now();
    }

    private void apply(ProviderConfig c, ProviderConfigRequest req) {
        c.providerCode = req.providerCode();
        c.providerType = ProviderType.valueOf(req.providerType());
        if (req.priority() != null) c.priority = req.priority();
        if (req.enabled() != null) c.enabled = req.enabled();
        c.credentialsRef = req.credentialsRef();
        if (req.supportedLanguages() != null) c.supportedLanguages = String.join(",", req.supportedLanguages());
        if (req.supportedVoices() != null) c.supportedVoices = String.join(",", req.supportedVoices());
        c.costLimitCents = req.costLimitCents();
        if (req.timeoutMs() != null) c.timeoutMs = req.timeoutMs();
        c.fallbackProviderCode = req.fallbackProviderCode();
    }
}
