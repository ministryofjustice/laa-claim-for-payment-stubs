# X‑Auth Claim Enrichment Library

This library provides a way to **preserve and propagate user or business context** across service boundaries when using OAuth2 access tokens, without altering or weakening the primary authentication and authorisation model.

It is designed to work with Spring Security’s resource server support and introduces an optional secondary token, carried in the `X-Auth` header, whose claims can be safely merged into the authenticated principal.


---

## The problem

In a typical microservice setup:

- The **access token** (`Authorization: Bearer …`) proves *who is allowed* to call the API.
- However, access tokens are often issued to **services**, not end‑users.
- As requests pass through multiple services, **user‑level context** (for example user identifiers, firm codes, or roles) can be lost if enrichment is not available to all services requesting tokens.

Without an explicit mechanism for carrying that context:
- Requests authenticate successfully.
- Controllers are entered.
- But important claims are missing.


---

## High‑level approach

The library introduces an **optional second JWT**, supplied via the `X-Auth` header:

- The access token remains the **only token used for authentication**.
- The X‑Auth token is used **only for additional, allow‑listed claims**.
- Those claims are merged into the authenticated principal *after authentication succeeds*.

No additional trust is granted and no new authorities are derived from X‑Auth.

---

## How the pieces fit together

### 1. Normal authentication (unchanged)

Spring Security processes the incoming request as usual:

- The access token from the `Authorization` header is decoded and validated.
- Scopes / authorities are derived from the access token.
- Method security and request authorisation behave exactly as before.

This library does not interfere with that process.

---

### 2. `XAuthHeaderResolver`

`XAuthHeaderResolver` is a small abstraction responsible for locating the X‑Auth token for the current request.

Its role is deliberately narrow:

- Resolve the raw X‑Auth header value, if present.
- Hide any servlet‑specific access (such as `RequestContextHolder`) from the rest of the security logic.
- Allow alternative resolution strategies in future if needed.

If no X‑Auth header is present, the library simply does nothing further.

---

### 3. `XAuthJwtAuthenticationConverter`

`XAuthJwtAuthenticationConverter` is the core integration point.

It wraps an existing `JwtAuthenticationConverter` and follows this flow:

1. Delegate to the standard converter to produce the initial `JwtAuthenticationToken`.
2. Ask the `XAuthHeaderResolver` whether an X‑Auth token is present.
3. If present:
   - Decode the X‑Auth JWT using a dedicated decoder.
   - Extract claims via `XAuthClaimsExtractor`.
   - Filter those claims against an explicit allow‑list.
   - Merge the allowed claims into the existing JWT claims.
4. Return a single enriched `JwtAuthenticationToken`.

The result is still *one authenticated principal*, but with additional context attached.

---

### 4. Claim allow‑listing

Only a fixed, explicit set of claims is permitted to flow from X‑Auth into the principal.

This avoids:
- Trusting issuer‑specific or unexpected claims.
- Accidentally overriding core JWT fields such as `sub`, `iss`, or `aud`.
- Turning X‑Auth into a secondary authorisation mechanism.

The access token remains authoritative for all security decisions.

---

## What this library deliberately does not do

- It does **not** authenticate requests using X‑Auth.
- It does **not** grant authorities or scopes from X‑Auth claims.
- It does **not** replace or bypass Spring Security’s resource server.
- It does **not** expose two separate authentication objects.

X‑Auth is treated strictly as *context*, not *identity*.

---

## Design philosophy

This approach ensures that:

- Authentication and authorisation stay simple and standards‑compliant.
- Context propagation is explicit rather than implicit or assumed.
- Missing X‑Auth headers fail safely (claims are absent, not invented).
- Services can reason independently about what extra claims they trust.

The end result is clearer behaviour, easier debugging, and fewer hidden dependencies between services.

