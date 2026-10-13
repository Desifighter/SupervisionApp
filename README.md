# SupervisionAPP

SupervisionAPP is a local-first Android supervision platform designed around what Android actually allows on a real managed device. It is not a generic screen-time reminder app and it does not pretend to provide protection when the platform does not allow it.

This repository contains the Phase 0 diagnostics lab, the Phase 1 consent gate, the Phase 2 policy-evaluation layer, the Phase 3 guarded action gate, and the Phase 4 local persistence/reporting layer. It is positioned as a guarded observability platform rather than an unsupported enforcement product.

## Why this exists

The project started from a strict requirement: build a supervision application that works with real Android constraints instead of assumptions.

On a managed device, especially one with a Work Profile and corporate security tooling, the app must be honest about:

- what it can observe
- what it cannot control
- what it requires from Android policy and device management
- what is safe to claim in a production release

This is why the first version is diagnostic-only and intentionally transparent.

## Core product idea

SupervisionAPP is intended to provide a structured supervision and monitoring workflow for a managed mobile environment, with the following principles:

- local-first processing
- no fake or misleading security claims
- explicit capability checks at runtime
- strong privacy boundaries
- conservative enforcement only when Android grants the required access
- support for managed-device diagnostics before enforcement

## Product phases

### Phase 0 — Diagnostics Lab (completed)

This phase proves whether the Android environment supports the required supervision primitives.

The app checks:

- Android version and API level
- Usage Access status
- Accessibility status
- current foreground app
- profile type (Personal vs Work)
- VPN state
- device owner / profile owner status
- package visibility restrictions
- monitoring state

This phase is intentionally read-only. It does not manipulate the Work Profile, disable apps, or claim enforcement capability.

### Phase 1 — Safe observability and consent (completed)

Implemented features:

- structured onboarding and consent flow
- capability-safe app entry flow
- explicit warning that the app is observation-only
- governed access to the diagnostics experience

### Phase 2 — Policy engine and rules (completed)

Implemented features:

- policy profiles and rule evaluation
- local rule evaluation for readiness, block, and unsupported states
- policy status display in the diagnostics UI
- safe reporting of device capability limits without claiming enforcement

### Phase 3 — Guarded action gate (completed)

Implemented features:

- runtime capability gate for actionable enforcement paths
- READY, BLOCKED, and UNSUPPORTED evaluation states
- explicit message when enforcement is not safe on the current device
- no claim of active enforcement unless the managed-device requirements are met

### Phase 4 — Local persistence and reporting (completed)

Implemented features:

- local audit trail for capability checks and policy decisions
- summary counts for READY, BLOCKED, and UNSUPPORTED events
- readable local report export for review and debugging
- safe storage of decision history without making unsupported control claims
- review artifacts for deployment readiness, executive briefings, and recommendation guidance

### Phase 5 — Production-readiness and managed deployment (review gate)

This phase is intentionally documented as a gating step rather than a claim of enforcement.

Required before any deployment beyond a lab or review environment:

- verified managed-device or profile-owner support
- explicit enterprise policy review
- privacy and compliance sign-off
- deployment model aligned to the actual Android enforcement capabilities on the target fleet
- evidence that the app is only allowed to act where the platform grants the required authority

## Current status

The project is in a verified, review-ready, capability-safe state. Phases 0 through 4 are complete and validated on the Moto G85 test environment.

Verified findings from the live lab:

- app builds successfully
- app installs successfully
- app launches successfully
- usage access is enabled
- accessibility is enabled
- app is detected in the personal profile
- the device has a Work Profile, but the app is not installed or enforced there
- the guarded action gate correctly reports READY, BLOCKED, and UNSUPPORTED capability states
- the local audit trail and local report summary are implemented and pass tests
- no real production enforcement should be claimed from this environment

This is the correct result for a diagnostics-first, capability-safe implementation. It prevents false product claims and keeps the managed environment intact.

## Architecture

The project is structured around a conservative, layered design:

### Presentation layer

- Jetpack Compose UI
- Material 3 theming
- diagnostics dashboards and status screens

### Android capability layer

- UsageStatsManager
- AccessibilityService
- DevicePolicyManager
- UserManager
- ConnectivityManager
- package visibility checks

### Domain layer

- policy model
- capability checks
- state interpretation
- supportability evaluation

### Data layer

- local persistence for diagnostics and policy state
- future Room integration for usage records and configuration

### Enforcement layer

- intentionally off by default in Phase 0
- only available when platform support and device policy are verified

## Repository structure

```text
.
├── app/
│   ├── src/
│   └── build.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── settings.gradle.kts
├── README.md
├── .gitignore
└── local.properties
```

## Build and run

### Prerequisites

- Java 17+
- Android SDK installed locally
- Android platform 36 available
- Android Studio or command-line SDK tools configured

### Build

```bash
./gradlew assembleDebug
```

### Install

```bash
./gradlew installDebug
```

### Launch the app

```bash
adb shell am start -n com.supervisionapp.lab/.MainActivity
```

## Important limitation

This project does not assume device-owner or profile-owner privileges. On a managed phone or work-profile environment, Android may intentionally restrict what a normal app can observe or control.

Because of that, this repository is intentionally designed to:

- validate capability before claiming enforcement
- avoid false security promises
- keep the Work Profile and managed environment untouched
- build a safe foundation for later phases

## Remaining production gate

The project is intentionally not claiming a full enterprise enforcement product yet. The remaining work is concentrated in the contract between Android capabilities and deployment reality:

1. managed-device support must be present and verified before enforcement is attempted
2. a privacy and compliance review must be completed for any real-world rollout
3. enterprise deployment documentation should be produced only for supported environments
4. production launch should remain blocked until the review gate confirms real ability and policy alignment

## License

This project is licensed under the MIT License.

## Roadmap

1. Phase 0 diagnostics validation — complete
2. Safe consent and app-policy onboarding — complete
3. Policy engine and rule evaluation — complete
4. Guarded action gate — complete
5. Local persistence and reporting — complete
6. Deployment readiness review gate — in progress, documentation-first only
7. Managed-device enforcement rollout — pending real Android policy support
