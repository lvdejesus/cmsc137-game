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

.PHONY: all build run clean

all: run

build:
	@mkdir -p $(OUT)
	javac -cp "$(CLASSPATH)" -d $(OUT) $(SOURCES)

run: build
	java -cp "$(CLASSPATH)" \
		-Djava.library.path="$(NATIVE_PATH)" \
		client.Main

clean:
	rm -rf $(OUT)
