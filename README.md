# PVCPipe

Work in progress. Not ready for release.

Requires Android 8.0 (API 26) or newer.

PVCPipe is a fork of [BravePipe](https://github.com/bravepipeproject/BravePipe),
created by [evermind](https://github.com/evermind-zz). BravePipe is based on
[NewPipe](https://github.com/TeamNewPipe/NewPipe).

## Repository layout

- `app/` contains the Android application.
- `pvcpipe-extractor/` contains the bundled extractor and its tests.
- [`docs/extractor-services.md`](docs/extractor-services.md) explains how URLs flow from the app
  through service link handlers and extractors.

Gradle substitutes the extractor dependency with the bundled composite build, so
the application and extractor are built together without publishing an extractor
artifact.

Local verification: `make ci` runs application checks and APK builds plus the
bundled extractor's deterministic regression tests and Checkstyle verification.

GPL-3.0-or-later; see [LICENSE](LICENSE).
