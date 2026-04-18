SRC = src
OUT = out
LIB = lib

LWJGL = $(LIB)/lwjgl.jar:$(LIB)/lwjgl-glfw.jar:$(LIB)/lwjgl-opengl.jar:$(LIB)/lwjgl-stb.jar:$(LIB)/joml-1.10.8.jar:$(LIB)/joml-primitives-1.10.0.jar
SOURCES := $(shell find $(SRC) -name "*.java")

all: run

build: $(OUT)/Main.class

$(OUT)/.build_stamp: $(SOURCES)
	@mkdir -p $(OUT)
	javac -cp "$(LWJGL)" -d $(OUT) $(SOURCES)
	@touch $(OUT)/.build_stamp

run: $(OUT)/.build_stamp
	java -cp $(LWJGL):$(OUT) -Djava.library.path=$(LIB)/natives/linux/x64 Main

clean:
	rm -rf $(OUT)
