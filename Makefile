EPUB_DIR := ./epub
EPUB_FILES := $(wildcard $(EPUB_DIR)/*)

.PHONY: run
run: build
	for file in $(EPUB_FILES) ; do \
		docker compose run --rm translator --src /app/epub/$$(basename $$file) ; \
	done

.PHONY: build 
build:
	docker compose build

# .PHONY: clean 
# 	docker compose down
# 	docker con
# 	docker rmi translator