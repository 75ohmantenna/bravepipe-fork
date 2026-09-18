# BravePipe-fork

Work in progress. Not ready for release.

Requires Android 8.0 (API 26) or newer.

Based on [BravePipe](https://github.com/bravepipeproject/BravePipe).

## Repository layout

- `app/` contains the Android application.
- `bravepipeextractor-fork/` contains the bundled extractor and its tests.
- [`docs/extractor-services.md`](docs/extractor-services.md) explains how URLs flow from the app
  through service link handlers and extractors.

Gradle substitutes the extractor dependency with the bundled composite build, so
the application and extractor are built together without publishing an extractor
artifact.

Local verification: `make ci`.

GPL-3.0-or-later; see [LICENSE](LICENSE).
