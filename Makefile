SRC = src
OUT = out
LIB = lib

# Finds all .java files
SOURCES := $(shell find $(SRC) -name "*.java")

# Dynamically builds the classpath using all .jar files found in LIB
# The 'sed' command removes the trailing colon
CLASSPATH := $(shell find $(LIB) -name "*.jar" | tr '\n' ':').:$(OUT)

# Ensure the native path matches your actual folder structure
NATIVE_PATH = $(LIB)/natives/x64/linux

.PHONY: all build run clean genmap

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
	@cp -r $(OUT)/* build/jar/
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

clean:
	rm -rf $(OUT) ./out/cmsc137-game.jar
