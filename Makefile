.PHONY: ci

ci:
	./gradlew --no-continue :app:runCheckstyle :app:runKtlint :app:checkDependenciesOrder \
		:app:lintDebug :app:testDebugUnitTest \
		:bravepipeextractor-fork:extractor:forkCiTest
	./gradlew --no-continue :app:assembleDebug :app:assembleRelease
