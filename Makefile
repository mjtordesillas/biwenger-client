.PHONY: \
	backend\:test backend\:deploy \
	android\:build android\:clean android\:test android\:test-connected \
	android\:install android\:uninstall android\:reinstall android\:launch

## backend/ — thin wrappers over backend/package.json's npm scripts

backend\:test:
	npm --prefix backend test

backend\:deploy:
	npm --prefix backend run deploy

## android/ — delegates to android/Makefile, which already has these
## targets (kept there too, since that's still the right place to run
## them from when working inside android/ directly)

android\:build:
	$(MAKE) -C android build

android\:clean:
	$(MAKE) -C android clean

android\:test:
	$(MAKE) -C android test

android\:test-connected:
	$(MAKE) -C android test-connected

android\:install:
	$(MAKE) -C android install

android\:uninstall:
	$(MAKE) -C android uninstall

android\:reinstall:
	$(MAKE) -C android reinstall

android\:launch:
	$(MAKE) -C android launch
