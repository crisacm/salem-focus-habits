# Pending tasks

Maintainer checklist of things that were left as placeholders or TODOs during
the fork of Loop Habit Tracker. Check items off as they are completed.

## Project contact

- [ ] Create a project email address and use it in:
  - `uhabits-android/src/main/play/contact-email.txt` (currently the placeholder `TODO-contact-email@example.invalid`)
  - `bugReportTo` in `uhabits-android/src/main/res/values/constants.xml` (currently empty, so the user chooses the recipient)
  - optionally `SECURITY.md`
- [ ] If the project gets a website, set it in `uhabits-android/src/main/play/contact-website.txt` (currently the GitHub repository)

## Privacy policy

- [ ] Write Salem's own privacy policy (Google Play and the App Store require one)
- [ ] Point `privacyPolicyURL` in `constants.xml` to it (currently the README)

## GitHub repository settings (manual)

- [ ] Enable Discussions, with categories named `ideas` and `q-a` (used by `.github/ISSUE_TEMPLATE/config.yml`)
- [ ] Enable Private vulnerability reporting (used by `SECURITY.md`)
- [ ] Optionally set the repository description and topics

## Store assets and screenshots

- [ ] Salem icon, feature graphic and phone screenshots in `uhabits-android/src/main/play/listings/en-US/graphics/` (currently empty)
- [ ] Screenshots for the README (the section is commented out) and the `screenshots/` folder (currently empty)

## App identity

- [ ] Replace the launcher icon and branding (still Loop's)
- [ ] Set `playStoreURL` to `market://details?id=io.github.crisacm.salem` once the app is published
- [ ] Prepare the App Store listing and metadata for iOS

## Localization

- [ ] Only the `en-US` and `es-rES` Play listings remain; add more languages if needed
- [ ] Translated strings such as `intro_description_1` still mention Loop
- [ ] Set up a translation platform, then update `docs/GUIDELINES.md` and `translateURL`

## Code

- [ ] Rename the Kotlin packages and `namespace` from `org.isoron.uhabits`, and the Gradle modules `uhabits-*` (planned for the KMP migration)
- [ ] Decide what to do with the `org.isoron.uhabits.ACTION_*` intent actions and the `content://org.isoron.uhabits/...` URI (kept for Tasker compatibility)
- [ ] Sync server: the default URL is empty; set up a server or remove the setting
- [ ] Review `versionCode` and `versionName`
- [ ] Replace the iOS stub actuals in `uhabits-core/src/appleMain` with real implementations (steps 3 and 4) and the `TODO` stubs in `uhabits-core/src/appleTest`, so the whole commonTest suite can run on `iosSimulatorArm64Test`
- [ ] iOS database: opening a file that is not a SQLite database fails with a cryptic sqliter error (`error while compiling: PRAGMA journal_mode`); wrap it with a clear message. `peekUserVersion` reads the file header, which can be stale for a database in WAL mode whose latest `user_version` is only in the `-wal` file (the app database uses a single journal file, importers open copies)
- [ ] Upgrade AGP to 8.10 or later and migrate the core Android target from `androidTarget` to the `com.android.kotlin.multiplatform.library` plugin (the upgrade also silences the D8 Kotlin metadata warnings)

## Documentation

- [ ] Add iOS and KMP instructions to `docs/BUILD.md` and `docs/TEST.md`
- [ ] Add a `CODE_OF_CONDUCT.md`
- [ ] Add Salem's own features to the Features section of the README (the current list is inherited from Loop and marked with `*`)
- [ ] Keep the Salem section of `CHANGELOG.md` up to date

## Release and CI

- [x] Add CI workflow (`.github/workflows/ci.yml`: assemble, core JVM tests, Android unit tests and ktlint on PRs to `develop`)
- [ ] Confirm the first GitHub Actions run of `ci.yml` is green
- [ ] `WidgetPreferences` keys (`widget-%06d-habit`, `snooze-%06d`) still use the locale-sensitive `format`; on devices whose locale uses non-Latin digits (for example Arabic or Persian) the stored keys contain those digits. Changing them to `formatInvariant` would orphan existing widget settings on those devices, so it needs a data migration first
- [ ] Set up signing and publishing for Google Play and the App Store
