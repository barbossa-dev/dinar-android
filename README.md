# Dinar for Android

A Persian personal finance app built with Kotlin and Jetpack Compose, following the
[Now in Android](https://github.com/android/nowinandroid) architecture.

## Getting started

1. Open the `dinar-android` folder in Android Studio (Narwhal or newer).
2. Let Gradle sync (JDK 17+).
3. Run the `app` configuration on an emulator or device (Android 8.0+).

```bash
./gradlew :app:assembleDebug      # debug build
./gradlew testDebugUnitTest       # unit tests
./gradlew :app:bundleRelease      # AAB for Google Play
```

Dependencies resolve through the Myket mirror (`https://maven.myket.ir`), configured in
`settings.gradle.kts` and `build-logic/settings.gradle.kts`. Library versions live in
`gradle/libs.versions.toml`.

### Troubleshooting: `zip END header not found`

This means a JAR in the local Gradle cache is truncated or corrupted, usually from an
interrupted download. Delete the broken file (or the whole `~/.gradle/caches/modules-2`
folder), then sync again so Gradle downloads it fresh through the mirror.

## Architecture

```
app/                    App shell: MainActivity, adaptive navigation suite, NavHost
build-logic/            Gradle convention plugins (as in NIA)
core/
  model/                Domain models, pure Kotlin with no Android dependency
  common/               Jalali calendar, money/date formatting, dispatchers
  database/             Room: entities, DAOs, seed data
  datastore/            User preferences with DataStore
  data/                 Repositories (single source of truth, offline-first)
  domain/               Use cases and pure aggregations (reports, budgets, dashboard)
  designsystem/         Material 3 light/dark theme, icons, charts, components
  ui/                   Shared model-aware UI (transaction row, Jalali date picker)
feature/
  home/                 Dashboard: balance, accounts, recent transactions
  transactions/         Monthly list, search, filters, delete with undo
  editor/               Add/edit transactions with a numeric keypad
  reports/              Monthly and yearly reports, donut and bar charts
  budgets/              Monthly budget per category
  settings/             Theme, currency unit, Persian digits, accounts, CSV export
```

- **Unidirectional data flow:** DAO → Repository → UseCase → ViewModel (`StateFlow`) → Compose UI.
- **Hilt** for dependency injection and **type-safe Navigation** routes with `kotlinx.serialization`.
- Amounts are always stored in **rial** as `Long` and converted to toman or rial only for display.
- The Jalali calendar is pure Kotlin and unit tested (day-by-day round trip over 30 years).
- The whole UI is right-to-left and follows Material 3: navigation bar on phones and rail on
  tablets, FABs, segmented buttons, bottom sheets, snackbars with undo, and dynamic color on
  Android 12+.

## Font

The UI uses Vazirmatn (SIL Open Font License), bundled in `core/designsystem/src/main/res/font/`.

## Store assets

`store/` holds the listing graphics: 1080×1920 phone screenshots, the 1024×500 feature graphic
and the 512×512 icon, for Google Play, Myket and Cafe Bazaar.

## Release builds

The release build is minified and resource-shrunk with R8 in full mode, strips debug logging,
keeps line numbers for crash reports, and only ships Persian and English resources.
App-specific R8 rules live in `app/proguard-rules.pro`.

1. **Upload key:** already created at `keystore/dinar-upload.jks` (SHA-256
   `72:DB:85:4B:…:F2:BA`). To make a new one, run `scripts/create-upload-keystore.sh`. It writes `keystore/dinar-upload.jks` and `keystore.properties` (both git-ignored).
   **Back them up**: without this key you cannot publish updates.
2. **Set the version** in `gradle.properties` (`dinar.versionCode` must grow with every upload).
3. **Build:**
   ```bash
   scripts/build-release.sh
   ```
   It runs the unit tests, then writes to `release/`:
   - `dinar-<version>.aab` for Google Play
   - `dinar-<version>.apk` for Myket, Cafe Bazaar and direct installs
   - `mapping-<version>.txt` to upload to the store for readable crash reports

On CI, provide the key through `DINAR_KEYSTORE_FILE`, `DINAR_KEYSTORE_PASSWORD`,
`DINAR_KEY_ALIAS` and `DINAR_KEY_PASSWORD` instead of `keystore.properties`.
Without a key, release builds fall back to the debug key and print a warning; never publish those.

The application ID is `ir.famastudio.dinar`, the same as the iOS bundle ID.
Dinar asks for no permissions and has no network access; all data stays on the device.

## Before publishing

- Replace the vector coin launcher icon with your final store icon if you have one.
- Prepare store screenshots in both light and dark themes.
