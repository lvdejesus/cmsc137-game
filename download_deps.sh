cd lib
wget https://build.lwjgl.org/addons/joml-primitives/joml-primitives-1.10.0.jar
wget https://build.lwjgl.org/addons/joml/joml-1.10.8.jar
wget https://build.lwjgl.org/release/3.4.1/bin/lwjgl/lwjgl.jar
wget https://build.lwjgl.org/release/3.4.1/bin/lwjgl/lwjgl-unsafe.jar
wget https://build.lwjgl.org/release/3.4.1/bin/lwjgl/lwjgl-natives-windows.jar
wget https://build.lwjgl.org/release/3.4.1/bin/lwjgl/lwjgl-natives-linux.jar
wget https://build.lwjgl.org/release/3.4.1/bin/lwjgl-stb/lwjgl-stb.jar
wget https://build.lwjgl.org/release/3.4.1/bin/lwjgl-stb/lwjgl-stb-natives-windows.jar
wget https://build.lwjgl.org/release/3.4.1/bin/lwjgl-stb/lwjgl-stb-natives-linux.jar
wget https://build.lwjgl.org/release/3.4.1/bin/lwjgl-opengl/lwjgl-opengl.jar
wget https://build.lwjgl.org/release/3.4.1/bin/lwjgl-opengl/lwjgl-opengl-natives-windows.jar
wget https://build.lwjgl.org/release/3.4.1/bin/lwjgl-opengl/lwjgl-opengl-natives-linux.jar
wget https://build.lwjgl.org/release/3.4.1/bin/lwjgl-glfw/lwjgl-glfw.jar
wget https://build.lwjgl.org/release/3.4.1/bin/lwjgl-glfw/lwjgl-glfw-natives-windows.jar
wget https://build.lwjgl.org/release/3.4.1/bin/lwjgl-glfw/lwjgl-glfw-natives-linux.jar
mkdir natives
ls | grep natives | xargs -n 1 -I{} mv {} natives
cd natives
ls | xargs -n 1 jar xf
cd windows/x64
find . -name *.dll | xargs -n 1 -I{} mv {} .
cd ../../linux/x64
find . -name *.so | xargs -n 1 -I{} mv {} .
