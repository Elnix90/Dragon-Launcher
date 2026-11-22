# Agent instructions

## Project

Dragon Launcher is an offline, privacy-first Android launcher built with Kotlin and Jetpack Compose. User data is stored locally and settings/backups are important compatibility surfaces. Do not add runtime internet access or telemetry.

Main modules:

- `app/`: Android application, manifest, build variants, and version metadata.
- `ui/`: Compose screens and presentation (`ui/main`, `ui/base`, `ui/theme`, `ui/composition`).
- `core/`: models, settings, permissions, shared code, and launcher services.
- `data/`: app/workspace repositories and local storage.
- `core/i18n/`: strings and shared Android resources.
- `build-logic/`: Gradle convention plugins.

Follow existing module boundaries and local patterns. The version catalog is `gradle/libs.versions.toml`; avoid scattering dependency versions.

## Non-negotiable safety and scope

- **Never build Release or Play Store variants.** Use Debug or Build Beta.
- Do not change `versionName` or `versionCode` unless asked.
- For device work, identify the intended device first and use `adb -s <serial>` on every command. Do not interact with other apps or the installed Release Dragon Launcher. Do not change HOME/default-app selection, display size/density/refresh settings, accessibility services, permissions, or other device settings.
- Install using `adb install -r` to preserve data. Never touch, uninstall, clear app data/cache the release app that is installed.
- Preserve existing user edits and data. Treat backups, serialized settings, and migrations as data-safety boundaries.
- Do not add permissions or dependencies without need. Never, NEVER add the `INTERNET` permission or network behavior.
- Ask before broad refactors or other large changes. Keep changes small and reviewable.

## Build and validation

- Use JDK 21, as in CI. The normal local build is `mise build` (or `mise install-debug`).
- Focused JVM tests: `mise test`. Run relevant tests when appropriate. Passing unit tests do not establish visual, device, or performance correctness.
- Before finishing the changes, validate the formatting using `mise format`.
- Keep profiling tools such as LeakCanary Debug-only. Do not include profiling instrumentation in production variants.

## Codebase notes

- Debug and Beta use separate application IDs and separate app data. Confirm which Dragon Launcher variant is active before device validation; installing Debug does not update Beta.

## Documentation and contribution rules

- [CONTRIBUTING.MD](CONTRIBUTING.MD)
- Never commit, never push, only a human can do that.
- `docs/` is a GitHub Pages/Jekyll site. New pages need front matter and a navigation entry in `docs/_data/nav.yml`. Keep `docs/privacy-policy.md` and its configured base URL stable.
