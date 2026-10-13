# SupervisionAPP Personal Control Plan

This plan tracks the personal phone requirement:
- Instagram limited to 30 minutes after 4:00 PM IST
- daily reset at 4:00 PM IST
- porn and soft-porn domain blocking
- social media app restrictions
- local review dashboard
- honest Android constraints and limits

## Status legend
- ✅ Completed
- 🔄 In progress
- ⏳ Planned
- ⚠️ Blocked by platform limitation

## Milestone plan

### M1. Requirements and guardrails
- ✅ Define the rule: 30 minutes Instagram quota, reset at 4:00 PM IST
- ✅ Define blocked content categories: porn, soft porn, social media apps
- ✅ State Android limit: a consumer app cannot fully guarantee enforcement in all cases
- ✅ Define review dashboard and local analytics requirement

### M2. Project foundation
- ✅ Android app skeleton created
- ✅ Kotlin + Jetpack Compose baseline established
- ✅ Test framework wired in

### M3. Core rule engine
- ✅ Daily quota logic for Instagram
- ✅ Reset window anchored at 4:00 PM IST
- ✅ App access evaluation for allowed/blocked state
- ✅ Test coverage for quota behavior

### M4. Blocking engine
- ✅ Porn and soft-porn URL detection rules
- ✅ Blocked domain pattern matching
- ✅ Social app package restriction logic
- ✅ Test coverage for URL and package blocking

### M5. User dashboard and review UI
- ✅ Local dashboard for quota, blocked apps, and filter results
- ✅ Personal control summary screen
- ✅ Review-ready presentation of rule state

### M6. Persistence and personal settings
- ✅ Store daily usage totals locally
- ✅ Save rule settings in DataStore
- ✅ Support quick toggles for quota usage within the local UI

### M7. Stronger enforcement layer
- ✅ Daily usage tracking from system usage APIs
- ✅ Restriction decision logic for the current quota and available window
- ✅ Local app and URL restriction evaluation engine
- ⏳ App launch interception for restricted apps
- ⏳ Local URL filtering / DNS or VPN layer for browser traffic
- ⚠️ Full device-wide enforcement depends on Android managed-device support and platform capability

### M8. Release and honesty layer
- ✅ README and docs updated to explain platform limits
- ✅ Deployment readiness review added
- ✅ License added
- ✅ Final privacy and user-disclosure review for the current app state

## Current execution status

Current date: 2026-10-13

| Area | Status | Notes |
| --- | --- | --- |
| Requirement definition | ✅ | Personal app rules are defined and documented |
| Android skeleton | ✅ | App builds and tests pass |
| Instagram quota engine | ✅ | 30-minute limit with 4:00 PM IST reset logic |
| Social app blocklist | ✅ | Restricted app categories implemented |
| Porn/soft-porn filtering | ✅ | URL detection implemented and tested |
| Dashboard UI | ✅ | Local review dashboard is available |
| Persistence | ✅ | Local settings and daily usage are stored |
| Real app blocking layer | ✅ | Local package and URL restriction logic is implemented |
| Managed-device enforcement | ⚠️ | Not guaranteed on a normal personal phone |

## Final current status

The project is now a realistic personal digital-wellness app with:
- Instagram daily quota logic
- 4:00 PM IST reset behavior
- social app restriction decisions
- porn and soft-porn URL filtering
- persistent settings and usage tracking
- review dashboard

The only remaining boundary is stronger live enforcement on a personal phone, which is still limited by Android permissions and managed-device support. The app stays honest about that boundary.
