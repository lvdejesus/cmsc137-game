SRC = src
OUT = out
LIB = lib

LWJGL = $(LIB)/lwjgl.jar:$(LIB)/lwjgl-glfw.jar:$(LIB)/lwjgl-opengl.jar:$(LIB)/lwjgl-stb.jar:$(LIB)/joml.jar:$(LIB)/joml-1.10.8.jar

all: run

build: $(OUT)/Main.class

$(OUT)/Main.class: $(SRC)/Main.java $(SRC)/SpriteBatch.java $(SRC)/Sprite.java $(SRC)/Camera.java $(SRC)/Texture.java $(SRC)/TextureAtlas.java
	javac -cp $(LWJGL) -d $(OUT) $^

run: $(OUT)/Main.class
	java -cp $(LWJGL):$(OUT) -Djava.library.path=$(LIB)/natives/linux/x64 Main

clean:
	rm -rf $(OUT)