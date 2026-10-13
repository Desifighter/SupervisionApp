# SupervisionAPP

SupervisionAPP is a local-first Android supervision platform designed around what Android actually allows on a real managed device. It is not a generic screen-time reminder app and it does not pretend to provide protection when the platform does not allow it.

This repository contains the Phase 0 diagnostics lab and the architecture foundation for the next phases of the product.

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

### Phase 0 — Diagnostics Lab (Current status)

This phase proves whether the Android environment supports the required supervision primitives.

The app currently checks:

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

### Phase 1 — Safe observability and consent

Planned features:

- structured onboarding and consent flow
- app and profile inventory reporting
- local audit logs
- safe, explainable monitoring state
- explicit warning when a capability is unavailable

### Phase 2 — Policy engine and rules

Planned features:

- policy profiles
- scheduled checks
- app-category rules
- local rule evaluation
- reporting for review and compliance

### Phase 3 — Guarded enforcement

Planned features:

- enforcement only behind verified capability gates
- runtime checks for permission and policy support
- no claims beyond the platform’s actual capabilities
- clear “unsupported on this device” states

### Phase 4 — Production-readiness and managed deployment

Planned features:

- enterprise deployment model
- stronger privacy and security review
- export and compliance reporting
- deployment patterns only where Android policy supports them

## Current status

The project is currently in Phase 0 and has been validated on the Moto G85.

Verified findings from the live lab:

- app builds successfully
- app installs successfully
- app launches successfully
- usage access is enabled
- accessibility is enabled
- app is detected in the personal profile
- the device has a Work Profile, but the app is not installed or enforced there
- no real production enforcement should be claimed from this environment

This is the right result for a diagnostics-first implementation. It prevents false product claims and keeps the managed environment intact.

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

## License

This project is currently being developed as a private internal prototype. Update this section before publishing a public release.

## Roadmap

1. Complete Phase 0 diagnostics validation
2. Add safe consent and app-policy onboarding
3. Formalize the rules engine
4. Add local persistence and reporting
5. Introduce guarded enforcement behind runtime capability checks
6. Prepare enterprise deployment documentation when platform capabilities are available
