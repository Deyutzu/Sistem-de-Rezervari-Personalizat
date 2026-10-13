package com.stayfinder.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

/**
 * Format UNIC pentru toate erorile API-ului.
 * Clientul (Android) poate parsa mereu același JSON, indiferent de tipul erorii.
 *
 * Exemplu:
 * {
 *   "timestamp": "2026-10-13T11:00:00Z",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "Parametri invalizi",
 *   "path": "/api/accommodations",
 *   "fieldErrors": { "minPrice": "trebuie să fie mai mare sau egal cu 0" }
 * }
 */
// NON_NULL: dacă nu există erori pe câmpuri, "fieldErrors" lipsește din JSON (în loc de null)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
}
