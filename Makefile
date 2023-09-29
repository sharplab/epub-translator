EPUB_DIR := ./epub
EPUB_FILES := $(wildcard $(EPUB_DIR)/*)

.PHONY: run
run:
	for file in $(EPUB_FILES) ; do \
		[ "$$(basename $$file)" != "$$(basename -- $$file)" ] && continue ; \
		docker run --rm \
			-v $$(pwd)/config:/app/config/ \
			-v $$(pwd)/epub:/app/epub/ \
			epub-translator --src /app/epub/$$(basename $$file) ; \
	done

.PHONY: build 
build:
	docker build -t epub-translator .

.PHONY: check
check: pull-submodule
	for file in $(EPUB_FILES) ; do \
		[ "$$(basename $$file)" != "$$(basename -- $$file)" ] && continue ; \
		docker run --rm \
			-v $$(pwd)/epub:/data \
			epub-checker $$(basename $$file) ; \
	done

.PHONY: build-checker
build-checker:
	docker build -t epub-checker ./epubcheck

.PHONY: pull-submodule
pull-submodule:
	 git submodule update --init --recursive

.PHONY: clean 
clean:
	docker rmi epub-translator epub-checker