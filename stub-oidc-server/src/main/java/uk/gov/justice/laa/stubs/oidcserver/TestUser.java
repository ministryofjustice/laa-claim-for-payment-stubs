package uk.gov.justice.laa.stubs.oidcserver;

import java.util.UUID;

/** Represents a test user profile with username, display name, email, and provider ID. */
public record TestUser(
    String username, String displayName, String email, String firmId, UUID providerUserId) {}
