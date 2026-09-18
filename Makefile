.PHONY: ci

ci:
	./gradlew --no-continue :app:runCheckstyle :app:runKtlint :app:checkDependenciesOrder \
		:app:lintDebug :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease \
		:bravepipeextractor-fork:extractor:forkCiTest
