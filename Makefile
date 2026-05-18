SRC = src
OUT = out
LIB = lib

SOURCES := $(shell find $(SRC) -name "*.java")
CLASSPATH := $(shell find $(LIB) -name "*.jar" | tr '\n' ':').:$(OUT)
NATIVE_PATH = $(LIB)/natives/linux/x64/

.PHONY: all build run clean genmap jar jar-lean-linux jar-lean-windows

all: run

genmap: build
	java -cp "$(CLASSPATH)" common.MapGeneratorTest

build:
	@mkdir -p $(OUT)
	javac -cp "$(CLASSPATH)" -d $(OUT) $(SOURCES)

run: build
	java -cp "$(CLASSPATH)" \
		-Djava.library.path="$(NATIVE_PATH)" \
		client.Main

run-editor: build
	java -cp "$(CLASSPATH)" \
		-Djava.library.path="$(NATIVE_PATH)" \
		editor.Main

jar: build
	@rm -rf build/jar
	@mkdir -p build/jar
	@echo "[JAR] Extracting dependency JARs..."
	@for jar in $(LIB)/*.jar; do \
		cd build/jar && jar xf ../../$$jar 2>/dev/null || true; \
		cd ../..; \
	done
	@rm -rf build/jar/META-INF
	@echo "[JAR] Copying compiled classes..."
	@cp -r $(OUT)/client build/jar/
	@cp -r $(OUT)/editor build/jar/
	@cp -r $(OUT)/common build/jar/
	@cp -r $(OUT)/framework build/jar/
	@cp -r $(OUT)/org build/jar/
	@echo "[JAR] Copying resources..."
	@cp -r res build/jar/res
	@echo "[JAR] Copying native libraries..."
	@mkdir -p build/jar/natives
	@cp -r lib/natives/* build/jar/natives/
	@echo "[JAR] Creating manifest..."
	@echo "Main-Class: client.Main" > build/MANIFEST.MF
	@cd build/jar && jar cfm ../../out/cmsc137-game.jar ../MANIFEST.MF .
	@rm -rf build ../MANIFEST.MF
	@echo "[JAR] Created cmsc137-game.jar"

jar-lean-linux: build
	@$(MAKE) _jar-lean \
		PLATFORM=linux \
		JAR_NAME=cmsc137-game-linux.jar \
		NATIVE_FILES="liblwjgl.so liblwjgl_opengl.so libglfw.so liblwjgl_stb.so"

jar-lean-windows: build
	@$(MAKE) _jar-lean \
		PLATFORM=windows \
		JAR_NAME=cmsc137-game-windows.jar \
		NATIVE_FILES="lwjgl.dll lwjgl_opengl.dll glfw.dll lwjgl_stb.dll"

_jar-lean:
	@rm -rf build/jar
	@mkdir -p build/jar
	@echo "[JAR-LEAN] Extracting runtime JARs (no sources/javadoc/openal)..."
	@for jar in $(LIB)/*.jar; do \
		case $$jar in \
			*-sources.jar|*-javadoc.jar|*openal*) ;; \
			*) cd build/jar && jar xf ../../$$jar 2>/dev/null || true; cd ../..;; \
		esac; \
	done
	@rm -rf build/jar/META-INF
	@echo "[JAR-LEAN] Copying compiled classes..."
	@cp -r $(OUT)/client build/jar/
	@cp -r $(OUT)/editor build/jar/
	@cp -r $(OUT)/common build/jar/
	@cp -r $(OUT)/framework build/jar/
	@cp -r $(OUT)/org build/jar/
	@echo "[JAR-LEAN] Copying resources..."
	@cp -r res build/jar/res
	@echo "[JAR-LEAN] Copying $(PLATFORM) natives..."
	@mkdir -p build/jar/natives/$(PLATFORM)/x64
	@for native in $(NATIVE_FILES); do \
		cp $(LIB)/natives/$(PLATFORM)/x64/$$native build/jar/natives/$(PLATFORM)/x64; \
	done
	@echo "[JAR-LEAN] Creating manifest..."
	@echo "Main-Class: client.Main" > build/MANIFEST.MF
	@cd build/jar && jar cfm ../../out/$(JAR_NAME) ../MANIFEST.MF .
	@rm -rf build ../MANIFEST.MF
	@echo "[JAR-LEAN] Created out/$(JAR_NAME)"

clean:
	rm -rf $(OUT) ./out/cmsc137-game.jar ./out/cmsc137-game-linux.jar ./out/cmsc137-game-windows.jar
