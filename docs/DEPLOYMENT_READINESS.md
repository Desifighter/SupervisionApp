# Deployment Readiness Review

This document captures the proper release gate for SupervisionAPP. The app is intentionally designed to avoid unsupported enforcement claims.

## Review principle

The app may only advance beyond the lab or review phase when Android and the target device configuration provide real authority for the functions the system claims to support.

## Required review checklist

### 1. Device management state
- Confirm whether the target device is personally owned or managed.
- Verify whether a Work Profile is present and whether the app is installed in the intended profile.
- Confirm the device owner or profile owner status for the target environment.

### 2. Android capability verification
- Usage access must be granted where monitoring is required.
- Accessibility support must be verified if the app needs to rely on accessibility paths.
- Policy and management APIs must be confirmed on the actual deployment target.

### 3. Privacy and compliance
- Confirm the enterprise policy for collection and retention.
- Review required disclosures for end users and administrators.
- Validate how local data is stored and whether export controls are required.

### 4. Safety checks
- Ensure the app does not claim protection it cannot technically provide.
- Keep the blocked and unsupported states visible in the review UI.
- Require an explicit sign-off before a product claim is made.

### 5. Rollout gate
- Only proceed with enterprise rollout when the app is demonstrably supported by the target Android environment.
- Keep a documented decision log for every release review.

## Current project posture

The current repository is a capability-safe review app rather than a full enforcement product. It is suitable for diagnostics, policy review, and controlled evaluation in aligned environments.

## Exit criteria for production movement

A production move is appropriate only when all of the following are true:

- the deployment target has verified managed-device support
- the required Android APIs are available and functioning
- the platform allows the intended supervision workflow without bypassing user protections
- the organization has approved privacy, security, and compliance review
- the team is prepared to state exactly what the product does and does not enforce
