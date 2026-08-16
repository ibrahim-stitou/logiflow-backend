package com.logiflow.tms.ai.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Requête de question posée au copilote conversationnel. */
public record CopiloteRequest(@NotBlank @Size(max = 2000) String question) {}
