EPUB_DIR := ./epub
EPUB_FILES := $(wildcard $(EPUB_DIR)/*)

.PHONY: run
run:
	for file in $(EPUB_FILES) ; do \
		[ "$$(basename $$file)" != "$$(basename -- $$file)" ] && continue ; \
		docker compose run --rm translator --src /app/epub/$$(basename $$file) ; \
	done

.PHONY: build 
build:
	docker compose build

.PHONY: check
check: pull-submodule
	for file in $(EPUB_FILES) ; do \
		[ "$$(basename $$file)" != "$$(basename -- $$file)" ] && continue ; \
		docker compose run --rm checker $$(basename $$file); \
	done

.PHONY: pull-submodule
pull-submodule:
	 git submodule update --init --recursive

# .PHONY: clean 
# 	docker compose down
# 	docker con
# 	docker rmi translator