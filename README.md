# BravePipe-fork

Work in progress. Not ready for release.

Based on [BravePipe](https://github.com/bravepipeproject/BravePipe).

## Repository layout

- `app/` contains the Android application.
- `bravepipeextractor-fork/` contains the bundled extractor and its tests.

Gradle substitutes the extractor dependency with the bundled composite build, so
the application and extractor are built together without publishing an extractor
artifact.

Local verification: `make ci`.

GPL-3.0-or-later; see [LICENSE](LICENSE).
