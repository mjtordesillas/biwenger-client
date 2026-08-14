ADB := $(HOME)/Android/Sdk/platform-tools/adb
PACKAGE := com.biwenger_client

.PHONY: build clean test test-connected install uninstall reinstall launch

build:
	./gradlew assembleDebug

clean:
	./gradlew clean

test:
	./gradlew test

test-connected:
	./gradlew connectedAndroidTest

install:
	./gradlew clean assembleDebug test
	$(ADB) install -r app/build/outputs/apk/debug/app-debug.apk
	$(ADB) shell am start -n $(PACKAGE)/$(PACKAGE).MainActivity

uninstall:
	$(ADB) uninstall $(PACKAGE)

reinstall: uninstall install

launch:
	$(ADB) shell am start -n $(PACKAGE)/$(PACKAGE).MainActivity
