package com.tili.livetranslation.dto;

import jakarta.validation.constraints.NotBlank;

public record SessionStateUpdateRequest(@NotBlank String state) {}
