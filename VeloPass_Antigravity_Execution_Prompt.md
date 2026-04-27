# VeloPass — Complete Execution Prompt & Handoff
**Single self-contained document. No external files required.**
**Version 5.0 — 2026 — For personal use only**

---

## Instructions for the Agent / IDE

You are the mission controller for a multi-agent software build.

Your task is to build **VeloPass** — a personal bicycle registration and maintenance platform — in full, from zero to deployed. This single file contains every specification you need. Do not look for any other file. Do not invent, assume, or deviate from what is written here.

**Read this entire document before writing a single line of code or spawning any agent.**

---

## Hard Constraints — Non-Negotiable

Violations must be flagged to the human, not silently worked around.

- **NO** Apple device, Mac, Xcode, or Apple Developer account at any stage
- **NO** Google Play Store — APK via Obtainium (GitHub Releases) + Orion Store only
- **NO** theft-reporting features of any kind — no `isStolen` field, no related UI
- **NO** legal declarations — use personal owner attestation only on all documents
- **NO** FCM alternatives — FCM for Android push, VAPID for PWA push, no UnifiedPush
- **NO** public bike profiles — all bike data is private (owner only)
- **NO** analytics SDK in v1.0
- **CONSISTENT** release keystore — never change it once the first APK is published
- **ONE** Appwrite project shared by Android and PWA — no separate backends
- `legalResidenceCode` is **always** explicitly confirmed by the user — never inferred from device locale

---

## Platform Summary

| Property | Value |
|---|---|
| Primary platform | Android — Native (Kotlin + Jetpack Compose + Material3) |
| Secondary platform | iPhone / iPad / PC — Progressive Web App (PWA) |
| Backend | Appwrite Cloud (GitHub Student Developer Pack — free) |
| PWA host | Cloudflare Pages (free tier) — `pwa/` subdirectory |
| Design system | Material You — Material Design 3 |
| Fonts | DM Sans (all UI) + DM Mono (registration numbers only) |
| Android distribution | Obtainium (GitHub Releases) + Orion Store — NOT Play Store |
| PWA distribution | Direct Cloudflare Pages HTTPS URL — no store |
| Push — Android | Firebase Cloud Messaging (FCM) — free Spark plan |
| Push — PWA | VAPID Web Push via Appwrite Messaging (iOS 16.4+) |
| PDF generation | pdfkit — Node.js 18 Appwrite Function, server-side |
| Total hard cost | €0 at every stage |

---

## Agent Roster

| Agent | Name | Responsibility |
|---|---|---|
| AG-01 | Backend Agent | Appwrite Cloud — collections, buckets, permissions |
| AG-02 | Android Agent | Kotlin + Compose — all screens, FCM, Room, CameraX |
| AG-03 | Functions Agent | 7 Node.js 18 Appwrite Functions |
| AG-04 | PWA Agent | Vanilla JS + MWC — all screens, Service Worker, VAPID |
| AG-05 | Deploy Agent | Cloudflare Pages — `_headers`, `manifest.json`, CI |

---

## Agent Spawn Order

Spawn agents in this exact sequence. Do not start a phase until the previous phase passes its acceptance criteria.

```
Phase 1  → AG-01 + AG-02  → Foundation
Phase 2  → AG-02 + AG-03  → Bike Registration
Phase 3  → AG-03          → BikeIndex
Phase 4  → AG-02 + AG-03  → Maintenance
Phase 5  → AG-02          → Documents Tab
Phase 6  → AG-03          → Reminders
Phase 7  → AG-04 + AG-05  → PWA Build
Phase 8  → AG-02 + AG-04  → Account Module
Phase 9  → AG-02 + AG-04  → Localisation
Phase 10 → All agents     → Polish & QA
Phase 11 → AG-05          → Release
```

---

## Acceptance Criteria Per Phase

| Phase | Must Pass Before Proceeding |
|---|---|
| 1 | Appwrite collections exist with correct fields and permissions. Android app compiles and renders the Dashboard with placeholder data. |
| 2 | Registration wizard completes end-to-end. Both reg. number formats generated, stored, and displayed. Registration Certificate PDF downloadable. |
| 3 | BikeIndex OAuth2 flow completes. Test bike cross-registered. Verify screen returns BikeIndex data. |
| 4 | Maintenance log created, edited, and marked complete. Calendar shows correct dot colours. Maintenance Record Card PDF generated on completion. |
| 5 | Documents tab lists all PDFs. View, download, and regenerate all function correctly. |
| 6 | FCM notification arrives on physical Android device within 24h of due date. VAPID push arrives in Chromium browser. |
| 7 | PWA installs to iPhone home screen from Safari. All 5 screens render at Compact, Medium, Expanded breakpoints. Offline shell loads without network. |
| 8 | Account module complete on Android and PWA. Language switch immediate. BikeIndex connect/disconnect cycles without error. Export ZIP downloads. Deletion removes all data. |
| 9 | All 9 languages render without missing keys on both platforms. PDFs generate in correct language. |
| 10 | TalkBack passes all screens. WCAG AA contrast passes. Signed APK verifies with release keystore. No hardcoded secrets in any file. |
| 11 | GitHub Release APK installable via Obtainium. PWA live at Cloudflare Pages URL and installable from Safari on iPhone. |

---

## 1. Environment Setup

### 1.1 Repository Scaffold

Create this structure before any other work:

```
velopass/
├── android/              ← AG-02
├── pwa/                  ← AG-04 + AG-05 (Cloudflare Pages root)
│   ├── index.html
│   ├── manifest.json
│   ├── sw.js
│   ├── _headers
│   ├── i18n/
│   │   ├── en.json
│   │   ├── fr.json
│   │   ├── nl.json
│   │   ├── de.json
│   │   ├── es.json
│   │   ├── it.json
│   │   ├── pt.json
│   │   ├── ja.json
│   │   └── zh-Hans.json
│   └── assets/
└── functions/            ← AG-03
    ├── generate-registration/
    ├── bikeindex-sync/
    ├── generate-pdf/
    ├── maintenance-reminders/
    ├── translate-proxy/
    ├── export-user-data/
    └── delete-user-data/
```

### 1.2 Required Environment Variables

> Never commit these to the repository. Store in GitHub Actions secrets and Appwrite Function environment variables.

| Variable | Used By | Description |
|---|---|---|
| `APPWRITE_ENDPOINT` | All | `https://cloud.appwrite.io/v1` |
| `APPWRITE_PROJECT_ID` | All | From Appwrite Cloud dashboard |
| `APPWRITE_API_KEY` | AG-03 | Server API key with full scope |
| `BIKEINDEX_CLIENT_ID` | AG-03 | BikeIndex OAuth2 client ID |
| `BIKEINDEX_CLIENT_SECRET` | AG-03 | BikeIndex OAuth2 client secret |
| `BIKEINDEX_TOKEN_ENCRYPT_KEY` | AG-03 | 32-byte AES-256 key for stored tokens |
| `GOOGLE_TRANSLATE_API_KEY` | AG-03 | Google Cloud Translation API v3 |
| `FCM_SERVICE_ACCOUNT_JSON` | AG-03 | Firebase service account JSON (stringified) |
| `VAPID_PUBLIC_KEY` | AG-03 + AG-04 | VAPID public key for Web Push |
| `VAPID_PRIVATE_KEY` | AG-03 only | Never sent to client under any circumstances |
| `RELEASE_KEYSTORE_B64` | AG-02 CI | Base64-encoded Android release keystore |
| `RELEASE_KEY_ALIAS` | AG-02 CI | Keystore key alias |
| `RELEASE_KEY_PASSWORD` | AG-02 CI | Key password |
| `RELEASE_STORE_PASSWORD` | AG-02 CI | Store password |

---

## 2. AG-01 — Backend Agent

### 2.1 Collections

Create exactly these 5 collections in Appwrite Cloud. Document-level security only (not collection-level). Do not add fields not listed here.

#### Collection: `users`

| Field | Type | Constraints | Notes |
|---|---|---|---|
| userId | string | PK, unique | Appwrite document ID |
| email | string | unique, required | Primary login |
| displayName | string | max 80 | |
| avatarUrl | string | optional | Appwrite Storage ref |
| nationalityCode | string | required | ISO 3166-1 α-2 e.g. `"BE"` |
| legalResidenceCode | string | required | ISO 3166-1 α-2 — always user-confirmed, never inferred |
| previousShortRegNumber | string | optional | Populated when `legalResidenceCode` changes |
| previousFullRegNumber | string | optional | Populated when `legalResidenceCode` changes |
| preferredLanguage | string | default `"en"` | BCP-47 |
| notificationsEnabled | boolean | default `true` | |
| pushTokenAndroid | string | optional | FCM device token |
| pushTokenWeb | string | optional | VAPID subscription JSON |
| bikeIndexToken | string | optional | AES-256 encrypted OAuth token |
| createdAt | datetime | auto | |
| updatedAt | datetime | auto | |

#### Collection: `bikes`

| Field | Type | Constraints | Notes |
|---|---|---|---|
| bikeId | string | PK, unique | |
| ownerId | string | FK → users | |
| shortRegNumber | string | unique, indexed | 7 chars — stored without hyphen |
| fullRegNumber | string | unique, indexed | |
| bikeIndexId | string | optional, indexed | |
| make | string | required, max 60 | |
| model | string | required, max 80 | |
| year | integer | 1900–present+1 | |
| frameColor | string | required | M3 colour token name |
| frameType | enum | required | `Road\|MTB\|Hybrid\|City\|BMX\|Cargo\|eBike\|Other` |
| frameMaterial | enum | optional | `Aluminium\|Carbon\|Steel\|Titanium\|Other` |
| serialNumber | string | optional, indexed | |
| description | string | max 500 | |
| photoUrls | string[] | max 8 | Appwrite Storage refs |
| purchaseDate | datetime | optional | |
| purchasePrice | float | optional | |
| registeredAt | datetime | auto | |
| updatedAt | datetime | auto | |

#### Collection: `maintenance_logs`

| Field | Type | Notes |
|---|---|---|
| logId | string | PK |
| bikeId | string | FK → bikes |
| ownerId | string | FK → users (denormalised) |
| maintenanceType | enum | `CHAIN_CLEAN\|BRAKE_CHECK\|TYRE_PRESSURE\|TYRE_REPLACE\|CABLE_CHECK\|GEAR_TUNE\|WHEEL_TRUE\|BEARING_GREASE\|FULL_SERVICE\|CUSTOM` |
| status | enum | `Scheduled\|InProgress\|Completed\|Skipped` |
| scheduledDate | datetime | required |
| completedDate | datetime | optional |
| performedBy | enum | `Self\|LocalShop\|AuthorisedDealer` |
| shopName | string | optional, max 100 |
| mileageAtService | integer | optional, km |
| cost | float | optional |
| notes | string | max 1000 |
| attachmentUrls | string[] | max 5 |
| pdfFormUrl | string | Appwrite Storage path |
| reminderSentAt | datetime | dedup guard |
| createdAt | datetime | auto |

#### Collection: `schedule_templates`

| Field | Type | Notes |
|---|---|---|
| templateId | string | PK |
| bikeId | string | FK → bikes |
| maintenanceType | enum | Matches log type |
| frequencyUnit | enum | `Days\|Weeks\|Months\|KilometreInterval` |
| frequencyValue | integer | e.g. value `1` + unit `Months` = monthly |
| nextDueDate | datetime | Computed after each completed log |
| reminderDaysBefore | integer | Default `7` |
| isActive | boolean | Default `true` |

#### Collection: `translation_cache`

| Field | Type | Notes |
|---|---|---|
| cacheId | string | PK |
| contentHash | string | unique — SHA-256(sourceText + targetLang) |
| sourceText | string | Original text |
| targetLang | string | BCP-47 |
| translatedText | string | Result from Google Translate |
| expiresAt | datetime | createdAt + 30 days |

### 2.2 Permissions Matrix

| Collection | Create | Read | Update | Delete |
|---|---|---|---|---|
| users | Self | Self | Self | Self |
| bikes | Any auth user | Owner only | Owner | Owner |
| maintenance_logs | Owner of bike | Owner | Owner | Owner |
| schedule_templates | Owner of bike | Owner | Owner | Owner |
| translation_cache | Server Function | Server | Server | Server |

### 2.3 Storage Buckets

| Bucket ID | Access | Max Size | Allowed Types |
|---|---|---|---|
| `bike-photos` | Private — owner + signed URL | 10 MB | `image/jpeg`, `image/png`, `image/webp` |
| `pdfs` | Private — owner + signed URL | 5 MB | `application/pdf` |
| `attachments` | Private — owner only | 10 MB | `image/*`, `application/pdf` |

---

## 3. AG-03 — Functions Agent

### 3.1 Function Registry

| Function ID | Runtime | Trigger | Purpose |
|---|---|---|---|
| `generate-registration` | Node.js 18 | HTTP POST | Generate both reg. numbers with atomic collision check |
| `bikeindex-sync` | Node.js 18 | HTTP POST + DB Event | BikeIndex OAuth2, cross-registration, token refresh |
| `generate-pdf` | Node.js 18 | HTTP POST | pdfkit PDF — returns 15-min signed Storage URL |
| `maintenance-reminders` | Node.js 18 | Schedule: daily 07:00 UTC | Scan templates; dispatch FCM + VAPID Web Push |
| `translate-proxy` | Node.js 18 | HTTP POST | Google Translate API proxy; 30-day cache |
| `export-user-data` | Node.js 18 | HTTP POST | GDPR export — ZIP of all records + files; 1-hour URL |
| `delete-user-data` | Node.js 18 | DB Event: `users.*.delete` | Cascading hard delete of all user data |

### 3.2 Registration Number Algorithm

Implement exactly as below. Do not modify the logic.

```js
// functions/generate-registration/src/main.js

const FRAME_SHORT = {
  Road:"R", MTB:"M", Hybrid:"H", City:"C",
  BMX:"B", Cargo:"G", eBike:"E", Other:"O"
};
const FRAME_FULL = {
  Road:"RD", MTB:"MT", Hybrid:"HB", City:"CT",
  BMX:"BX", Cargo:"CG", eBike:"EB", Other:"OT"
};
const MATERIAL = {
  Aluminium:"A", Carbon:"C", Steel:"S", Titanium:"T", Other:"O"
};

async function generateNumbers(user, bike, db) {
  const cc  = user.nationalityCode.toUpperCase();
  const buf = crypto.randomBytes(10);
  const r4  = Array.from({length:4}, (_,i) =>
    String.fromCharCode(65 + (buf[i] % 26))).join('');
  const ll  = Array.from({length:4}, (_,i) =>
    String.fromCharCode(65 + (buf[i+4] % 26))).join('');
  const nnn = String((buf[8]<<8 | buf[9]) % 1000).padStart(3, '0');
  const sfx = user.legalResidenceCode !== user.nationalityCode
    ? `/${user.legalResidenceCode.toUpperCase()}` : '';
  const short = `${cc}${FRAME_SHORT[bike.frameType]}${r4}${sfx}`;
  const full  = `${cc}-${FRAME_FULL[bike.frameType]}${MATERIAL[bike.frameMaterial ?? 'Other']}-${ll}-${nnn}${sfx}`;
  const [cs, cf] = await Promise.all([
    db.listDocuments('bikes', [Query.equal('shortRegNumber', short)]),
    db.listDocuments('bikes', [Query.equal('fullRegNumber',  full)]),
  ]);
  if (cs.total > 0 || cf.total > 0)
    return generateNumbers(user, bike, db); // re-roll on collision
  return { short, full };
}
```

> Short format: 26⁴ ≈ 456K combinations. Full: 26⁴ × 10³ ≈ 456M. Both checked atomically.

### 3.3 Registration Number Formats

| Format | Pattern | Example | Storage note |
|---|---|---|---|
| Short (7 chars) | `[CC][F][RRRR]` | `BER-X4KM` | Stored as `BERX4KM` — no hyphen; hyphen is display-only |
| Full (structured) | `[CC]-[TT][FF]-[LLLL]-[NNN]` | `BE-RDA-XKQW-047` | Stored as-is |
| Residence suffix | Append `/[RC]` to both | `BER-X4KM/DE` | Only when `legalResidenceCode ≠ nationalityCode` |

**Frame type codes:**

| Short | Full | Type |
|---|---|---|
| R | RD | Road |
| M | MT | Mountain (MTB) |
| H | HB | Hybrid |
| C | CT | City / Commuter |
| B | BX | BMX |
| G | CG | Cargo |
| E | EB | e-Bike |
| O | OT | Other |

**Frame material codes:**

| Code | Material |
|---|---|
| A | Aluminium |
| C | Carbon Fibre |
| S | Steel |
| T | Titanium |
| O | Other / Unknown |

### 3.4 `maintenance-reminders` — Logic

Runs daily at 07:00 UTC:

1. Query `schedule_templates` where `nextDueDate ≤ today + reminderDaysBefore` AND `isActive = true`
2. Skip any record where `reminderSentAt` is already set this cycle (dedup guard)
3. Compose notification in `users.preferredLanguage` — call `translate-proxy` if not English
4. Dispatch FCM payload to `pushTokenAndroid` AND/OR VAPID to `pushTokenWeb` if each is present
5. Stamp `reminderSentAt` on the template record
6. Appwrite Realtime subscription on the client surfaces a live "Upcoming" chip on the Dashboard

---

## 4. AG-02 — Android Agent

### 4.1 Tech Stack

| Layer | Library | Notes |
|---|---|---|
| Language | Kotlin 1.9+ | Coroutines + Flow |
| UI | Jetpack Compose + Material3 | Dynamic colour via Palette API seeded from active bike photo |
| Navigation | Navigation Compose | Type-safe nav graph |
| Local DB | Room (SQLite) | Offline-first — mirrors Appwrite schema |
| Network | Ktor Client | HTTP to Appwrite REST API |
| Images | Coil | Async from Appwrite Storage signed URLs |
| Push | Firebase Cloud Messaging | Free Spark plan; requires Google Play Services |
| Camera | CameraX | Photo capture in registration wizard step 3 |
| PDF viewer | AndroidPdfViewer / WebView | Render from signed Appwrite URL |
| Calendar | ContentResolver Calendar API | Insert maintenance reminders into system calendar |
| QR codes | ZXing Android Embedded | Generate QR for both reg. number formats |
| DI | Hilt | Dependency injection |
| Build | Gradle AGP 8+ | Signed APK for GitHub Releases |

> FCM requires Google Play Services. On de-Googled devices without it, push will not arrive. Appwrite Realtime in-app chips remain functional as fallback.

### 4.2 Screen Inventory

| Screen | Route | Key Notes |
|---|---|---|
| Onboarding | `onboarding` | 6 steps; nationality/residence flow on step 4 — Yes/No toggle then picker |
| Dashboard | `home` | Hero card with both reg. numbers; upcoming chips; Realtime subscription |
| Bike List | `bikes` | `LazyVerticalGrid`; FAB → registration wizard |
| Registration Wizard | `bikes/register` | 7 steps; CameraX on step 3; both number formats previewed on step 7 |
| Bike Detail | `bikes/{id}` | Tab row: Overview / Photos / Maintenance / Documents |
| Maintenance Calendar | `maintenance` | Monthly grid; dot colour indicators; `ModalBottomSheet` on day tap |
| Maintenance Detail | `maintenance/{id}` | Status chip; all fields; Generate Form button |
| Documents Archive | `documents` | `FilterChip` row; View / Download / Regenerate per item |
| Account / Profile | `profile` | 5 sections: My Profile / Preferences / Integrations / Data & Privacy / About |
| Language Picker | `profile/language` | 9 languages with native names and flag |
| Notification Settings | `profile/notifications` | Master toggle + per-type toggles + lead-time slider + test button |
| BikeIndex Connect | `profile/bikeindex` | Chrome Custom Tab OAuth2; 3 states: not connected / connected / token expired |
| Verify | `bikes/verify` | Serial number search; informational display of BikeIndex data only |

### 4.3 GitHub Actions — Signed APK Release

```yaml
# .github/workflows/release.yml
name: Release APK
on:
  push:
    tags: ["v*"]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: "17"
          distribution: "temurin"
      - name: Decode keystore
        run: |
          echo ${{ secrets.RELEASE_KEYSTORE_B64 }} \
            | base64 -d > android/app/release.keystore
      - name: Build signed APK
        working-directory: android
        env:
          KEY_ALIAS:      ${{ secrets.RELEASE_KEY_ALIAS }}
          KEY_PASSWORD:   ${{ secrets.RELEASE_KEY_PASSWORD }}
          STORE_PASSWORD: ${{ secrets.RELEASE_STORE_PASSWORD }}
        run: ./gradlew assembleRelease
      - uses: softprops/action-gh-release@v2
        with:
          files: android/app/build/outputs/apk/release/*.apk
```

> **CRITICAL:** The release keystore must be identical across all releases. Android refuses updates signed with a different key. Once Obtainium users install v1, the keystore is permanent. Never lose it.

---

## 5. AG-04 — PWA Agent

### 5.1 Tech Stack

| Layer | Technology | Notes |
|---|---|---|
| UI components | Material Web (`@material/web`) | Google's official M3 web components |
| Dynamic colour | `material-color-utilities` + Color Thief | Seed from bike photo → full M3 CSS custom properties |
| Routing | History API | SPA; no framework |
| Offline | Service Worker + Cache API | Cache shell + recent Appwrite API responses |
| Local storage | IndexedDB via `idb` wrapper | Offline persistence |
| Push | Web Push API + VAPID | Appwrite Messaging dispatches VAPID payloads |
| Camera | `MediaDevices.getUserMedia()` | Safari 14.3+; file `<input>` fallback on PC |
| PDF | Anchor download + inline `<iframe>` | Signed Appwrite Storage URLs |
| QR codes | `qrcode.js` CDN | Client-side generation |
| Fonts | DM Sans + DM Mono (Google Fonts) | DM Sans: all UI. DM Mono: registration numbers only. |
| i18n | Custom JSON loader | Keys mirror Android `strings.xml` names exactly |

### 5.2 Responsive Breakpoints

| Breakpoint | Width | Navigation | Layout |
|---|---|---|---|
| Compact | < 600 px | `md-navigation-bar` at bottom | Single column |
| Medium | 600–839 px | Bottom nav or navigation rail | Two-column bike card grid |
| Expanded | ≥ 840 px | Navigation rail — left, 80 dp wide | Nav rail + list panel + detail panel |

**Expanded layout (PC):**

```
┌─────────────────────────────────────────────────────┐
│  Header: VeloPass wordmark + search + avatar        │
├──────────┬──────────────────────┬───────────────────┤
│ Nav Rail │  List / Calendar     │  Detail Panel     │
│          │  (primary content)   │  (bike, log, doc) │
│ Home     │                      │                   │
│ Bikes    │                      │                   │
│ Maint.   │                      │                   │
│ Docs     │                      │                   │
│ Profile  │                      │                   │
└──────────┴──────────────────────┴───────────────────┘
```

**CSS skeleton:**

```css
md-navigation-bar { display: flex; }
.nav-rail         { display: none; }

@media (min-width: 840px) {
  md-navigation-bar { display: none; }
  .nav-rail {
    display: flex;
    flex-direction: column;
    width: 80px;
  }
  .content-layout {
    display: grid;
    grid-template-columns: 80px 1fr 1fr;
  }
}
```

### 5.3 Dynamic Colour

```js
import { themeFromImage } from '@material/material-color-utilities';

async function applyDynamicTheme(imgElement) {
  const theme = await themeFromImage(imgElement);
  const isDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
  const scheme = isDark ? theme.schemes.dark : theme.schemes.light;
  const root = document.documentElement.style;
  root.setProperty('--md-sys-color-primary',   hexFromArgb(scheme.primary));
  root.setProperty('--md-sys-color-secondary', hexFromArgb(scheme.secondary));
  // apply all 28 M3 colour tokens as CSS custom properties
}
// Default seed when no bike photo available: #1565C0
```

### 5.4 Desktop App Install (Chrome & Edge)

```js
let deferredPrompt;
window.addEventListener('beforeinstallprompt', e => {
  e.preventDefault();
  deferredPrompt = e;
  showInstallBanner();
});
document.getElementById('btn-install').addEventListener('click', async () => {
  if (!deferredPrompt) return;
  deferredPrompt.prompt();
  const { outcome } = await deferredPrompt.userChoice;
  if (outcome === 'accepted') hideInstallBanner();
  deferredPrompt = null;
});
// Firefox: no beforeinstallprompt — full PWA available in-browser; install prompt unavailable
```

---

## 6. AG-05 — Deploy Agent

### 6.1 Cloudflare Pages Settings

| Setting | Value |
|---|---|
| Build output directory | `pwa/` |
| Build command | *(none — vanilla JS, no build step)* |
| Root directory | `/` (repository root) |
| Node.js version | `18` |

### 6.2 `pwa/manifest.json`

```json
{
  "name": "VeloPass",
  "short_name": "VeloPass",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#1565C0",
  "theme_color": "#1565C0",
  "icons": [
    { "src": "/assets/icon-192.png", "sizes": "192x192", "type": "image/png" },
    { "src": "/assets/icon-512.png", "sizes": "512x512", "type": "image/png" },
    {
      "src": "/assets/icon-maskable.png",
      "sizes": "512x512",
      "type": "image/png",
      "purpose": "maskable"
    }
  ]
}
```

### 6.3 `pwa/_headers`

```
/*
  Content-Security-Policy: default-src 'self' https://*.appwrite.io; script-src 'self' https://cdn.jsdelivr.net; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com
  X-Frame-Options: DENY
  X-Content-Type-Options: nosniff
  Referrer-Policy: strict-origin-when-cross-origin
  Permissions-Policy: camera=(self), microphone=()
```

---

## 7. UI / UX Specification

### 7.1 Typography

| Role | Font | Weight | Size | Usage |
|---|---|---|---|---|
| Registration numbers | DM Mono | 700 | 28–42 sp | Both short and full reg. number displays |
| Form section title | DM Sans | 700 | 14–16 sp | Wizard step titles, card headers |
| Field label | DM Sans | 500 | 12 sp | Input field labels |
| Field input text | DM Sans | 400 | 16 sp | User-entered text in form fields |
| Helper / error text | DM Sans | 400 | 12 sp | Validation messages |
| Button label | DM Sans | 600 | 14 sp | All button text |
| Body text | DM Sans | 400 | 14 sp | Descriptions, general prose |
| Chip label | DM Sans | 500 | 12 sp | Status chips, filter chips |
| Caption | DM Sans | 400 | 11 sp | Timestamps, secondary metadata |

Import from Google Fonts. Android: downloadable font resources in `res/font/`. PWA: `<link>` in `index.html`.

### 7.2 Colour Tokens (Default Seed `#1565C0`)

| M3 Token | Light | Dark | Usage |
|---|---|---|---|
| `--md-sys-color-primary` | `#1565C0` | `#90CAF9` | Primary actions, FAB, active nav |
| `--md-sys-color-on-primary` | `#FFFFFF` | `#0D47A1` | Text/icons on primary |
| `--md-sys-color-secondary` | `#00897B` | `#80CBC4` | Secondary actions, teal accent |
| `--md-sys-color-surface` | `#FAFCFF` | `#0F1419` | Card and screen backgrounds |
| `--md-sys-color-surface-container` | `#E8EDF5` | `#1C2028` | Slightly elevated surfaces |
| `--md-sys-color-error` | `#B3261E` | `#F2B8B5` | Errors, validation |
| `--md-sys-color-outline` | `#72787E` | `#8C9198` | Borders, dividers |

### 7.3 Shape Tokens

| Token | Radius | Used On |
|---|---|---|
| Extra Small | 4 dp | Chips, small badges |
| Small | 8 dp | Text fields, small cards |
| Medium | 12 dp | Standard cards, dialogs |
| Large | 16 dp | Bottom sheets, hero cards |
| Full / Pill | 50% | FAB, icon buttons, avatars |

### 7.4 Maintenance Status Colours

| Status | Hex | Usage |
|---|---|---|
| Completed | `#10B981` | Green dot on calendar |
| Upcoming | `#F59E0B` | Amber dot on calendar |
| Overdue | `#EF4444` | Red dot on calendar |
| Scheduled | `#1565C0` | Blue chip |

### 7.5 Navigation — Both Platforms

| Tab | Icon | Content |
|---|---|---|
| Home | `directions_bike` | Dashboard — bike hero card, upcoming chips, FAB |
| My Bikes | `garage` | Bike grid/list — FAB to register |
| Maintenance | `build_circle` | Calendar + log list — FAB for manual entry |
| Documents | `folder_open` | PDF archive — filterable by bike |
| Profile | `account_circle` | Full Account module |

---

## 8. Onboarding & Registration Wizard

### 8.1 Onboarding Flow (6 Steps — Both Platforms)

1. Welcome — branding, tagline, language auto-detection prompt
2. Value proposition — three animated slides: Register / Maintain / Archive
3. Account creation — email + password, or Google OAuth2; Apple OAuth2 available server-side via Appwrite (no Apple SDK needed)
4. Profile setup — display name, then nationality/residence flow:
   - "What is your nationality?" → searchable country picker → stores `nationalityCode`
   - "Are you currently living in the same country?" → Yes/No toggle
   - If No: "Which country is your legal country of residence?" → picker → stores `legalResidenceCode`
   - If Yes: `legalResidenceCode` = `nationalityCode` automatically
5. Notification permission — Android: system dialog. PWA: Web Push fires on first subscription
6. First bike prompt — register now or skip to Dashboard

### 8.2 Registration Wizard (7 Steps)

| Step | Title | Fields |
|---|---|---|
| 1 | Basic Info | Make, Model, Year, Frame Type |
| 2 | Frame Details | Frame Colour, Frame Material, Serial Number (optional) |
| 3 | Photos | 1–8 photos; at least 1 full-side view required; CameraX on Android |
| 4 | Purchase Info | Purchase Date, Price, Currency (all optional) |
| 5 | Description | Free-text description, extra identifiers |
| 6 | BikeIndex | Opt-in cross-registration via OAuth2 (optional) |
| 7 | Review & Confirm | Full summary; both reg. number formats previewed side by side |

On confirmation: generate both numbers → save bike → BikeIndex sync if opted in → generate Registration Certificate PDF → push confirmation.

---

## 9. Maintenance Management

### 9.1 Maintenance Type Enum

| Code | Label | Default Frequency |
|---|---|---|
| `CHAIN_CLEAN` | Chain Cleaning & Lubrication | Every 4 weeks or 300 km |
| `BRAKE_CHECK` | Brake Pad Inspection & Adjustment | Every 8 weeks |
| `TYRE_PRESSURE` | Tyre Pressure Check | Every 2 weeks |
| `TYRE_REPLACE` | Tyre Replacement | Every 5,000 km |
| `CABLE_CHECK` | Cable & Housing Inspection | Every 6 months |
| `GEAR_TUNE` | Gear Indexing & Derailleur Tune | Every 3 months |
| `WHEEL_TRUE` | Wheel Truing | Every 6 months or after impact |
| `BEARING_GREASE` | Bottom Bracket / Headset Greasing | Annually |
| `FULL_SERVICE` | Full Professional Service | Annually |
| `CUSTOM` | Custom Item | User-defined |

---

## 10. Documents & PDFs

### 10.1 Available Document Types

| Form | Trigger | Contents |
|---|---|---|
| Registration Certificate | Auto on registration; re-generatable on demand | Both reg. numbers, QR code, owner attestation, bike details, BikeIndex link |
| Maintenance Record Card | Auto on log marked Completed | Type, date, mileage, performed by, shop info, notes, next due date |
| Annual Maintenance Summary | User-requested | All completed logs for chosen year; totals by type; total cost |

### 10.2 Registration Certificate Layout

| Section | Content |
|---|---|
| Header | VeloPass wordmark, "Personal Bicycle Registration Certificate", auto doc number, issue date |
| Short Code | Short reg. number large and bold (e.g. `BER-X4KM`) + QR code |
| Full Code | Full reg. number in DM Mono below the short code |
| Owner | Display name, nationality, legal country of residence |
| Bicycle Details | Make, Model, Year, Frame Type, Colour, Material, Serial (if provided) |
| BikeIndex | Record ID + link if cross-registered; else "Not cross-registered" |
| Photo | First uploaded bike photo — right-aligned thumbnail |
| Owner Attestation | *"I, [Name], confirm that the information above is accurate to the best of my knowledge."* + printed name line + signature line + date line |
| Footer | VeloPass GitHub URL, generation timestamp, "For personal use only" |

### 10.3 PDF Pipeline

```
1. User triggers (button or automatic)
2. Appwrite generate-pdf Function called: { bikeId, logId?, formType, targetLanguage }
3. Fetch data from Databases + first bike photo from Storage
4. pdfkit renders via localised Handlebars template for targetLanguage
5. Save to pdfs/ bucket (private, owner-only)
6. Return 15-minute signed URL to client
7. Store permanent Storage path on bike/log record for Documents tab
```

---

## 11. Account Module

### 11.1 Screen Structure

| Section | Item | Notes |
|---|---|---|
| My Profile | Edit display name + avatar | Circular crop; stored in `bike-photos` bucket |
| My Profile | Edit nationality | Searchable picker — updates `nationalityCode` |
| My Profile | Edit legal residence | Yes/No toggle + picker; prompts reg. number regen if changed; old numbers kept in `previous*` fields |
| My Profile | Change password | Email/password accounts only |
| Preferences | Language selector | 9 languages; native name + flag; takes effect immediately, no restart |
| Preferences | Notifications | Master toggle + per-type toggles + lead-time slider (1–30 days) + test button |
| Preferences | Appearance | Light / Dark / Follow System |
| Integrations | BikeIndex | 3 states: not connected / connected (username + bike list) / token expired |
| Data & Privacy | Export My Data | ZIP; 1-hour signed URL; `DownloadManager` on Android / browser download on PWA |
| Data & Privacy | Delete Account | Step 1: warning dialog → Step 2: user types `DELETE` → cascading hard delete |
| About | Links + version | Privacy Policy, Terms of Use, build version string |

### 11.2 Authentication

| Feature | Android | PWA |
|---|---|---|
| Email + password | ✅ | ✅ |
| Google OAuth2 | ✅ Google Sign-In SDK | ✅ redirect flow |
| Apple OAuth2 | ✅ server-side Appwrite | ✅ server-side Appwrite |
| Password reset | ✅ email link | ✅ email link |
| Session persistence | ✅ DataStore token | ✅ browser session cookie |
| Sign out current device | ✅ | ✅ |
| Sign out all devices | ✅ | ✅ |

> Apple OAuth2 is entirely server-side via Appwrite's OAuth2 provider. No Apple SDK, no Apple Developer account, no Mac required on either platform.

---

## 12. Localisation

### 12.1 Supported Languages

| Language | BCP-47 | Android path | PWA path |
|---|---|---|---|
| English | `en` | `res/values/strings.xml` | `i18n/en.json` |
| French | `fr` | `res/values-fr/strings.xml` | `i18n/fr.json` |
| Dutch | `nl` | `res/values-nl/strings.xml` | `i18n/nl.json` |
| German | `de` | `res/values-de/strings.xml` | `i18n/de.json` |
| Spanish | `es` | `res/values-es/strings.xml` | `i18n/es.json` |
| Italian | `it` | `res/values-it/strings.xml` | `i18n/it.json` |
| Portuguese | `pt` | `res/values-pt/strings.xml` | `i18n/pt.json` |
| Japanese | `ja` | `res/values-ja/strings.xml` | `i18n/ja.json` |
| Chinese (Simplified) | `zh-Hans` | `res/values-zh-rCN/strings.xml` | `i18n/zh-Hans.json` |

PWA: detect from `navigator.language`; fall back to `en.json` for missing keys. Language change takes effect immediately — no page reload.

### 12.2 Dynamic Translation — `translate-proxy`

Cache key: `SHA-256(sourceText + targetLang)`. TTL: 30 days. API key never leaves the server.

---

## 13. Security

### 13.1 APK Signing

```bash
# Run once — store output file permanently
keytool -genkey -v \
  -keystore velopass-release.keystore \
  -alias velopass \
  -keyalg RSA -keysize 2048 \
  -validity 10000
```

Store in password manager and encrypted offline backup. Never commit to repository. Use GitHub Actions secrets for CI.

### 13.2 GDPR

- **Export:** cascading ZIP of all user records + Storage files; 1-hour signed URL
- **Deletion:** two-step → cascading hard delete of all database records, Storage files, and Appwrite Auth account
- Privacy Policy + Terms of Use shown at onboarding and Settings > About

---

## 14. Development Roadmap

| Phase | Milestone | Lead | Deliverables | Weeks |
|---|---|---|---|---|
| 1 | Foundation | AG-01 + AG-02 | Appwrite setup, auth, user profile, nationality/residence flow, GitHub + CI, Android project, Compose + M3, i18n engine | 4 |
| 2 | Bike Registration | AG-02 + AG-03 | Registration wizard, dual number generation, CameraX, Registration Certificate PDF | 4 |
| 3 | BikeIndex | AG-03 | OAuth2, cross-reg API, Verify screen | 2 |
| 4 | Maintenance | AG-02 + AG-03 | Log CRUD, schedule templates, calendar, Maintenance Record Card PDF | 4 |
| 5 | Documents Tab | AG-02 | Archive screen, Annual Summary PDF, signed URLs, share/download | 2 |
| 6 | Reminders | AG-03 | FCM, VAPID Web Push, daily cron, Realtime chips | 2 |
| 7 | PWA Build | AG-04 + AG-05 | Service Worker, offline, MWC screens, Cloudflare Pages, 3-panel layout | 4 |
| 8 | Account Module | AG-02 + AG-04 | Full account screen both platforms | 2 |
| 9 | Localisation | AG-02 + AG-04 | 9 languages, translation proxy, PDF templates | 3 |
| 10 | Polish & QA | All | Motion, TalkBack, WCAG AA, signing, security | 3 |
| 11 | Release | AG-05 | GitHub Release APK, Orion listing, Obtainium README, PWA live | 2 |

**Total: 32 weeks. Total hard cost: €0.**

---

## On Completion

Report the following to the human before marking the build complete:

1. Cloudflare Pages live URL (PWA)
2. GitHub repository URL (for Obtainium users)
3. GitHub Release URL of the latest signed APK
4. Appwrite Cloud project ID
5. Any deviations from this document, with justification

Do not mark the build complete until all five items are confirmed live and accessible.

---

*VeloPass — Google Antigravity Execution Prompt v5.0 — 2026 — Personal use only*
