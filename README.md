<p align="center">
  <img src="menu_title.png" alt="Neon Drift Title" width="550"/>
  <br>
  <br>
  <b>A Multiplayer Top-Down 2D Rogue-like Shooter Built in Java</b>

</p>

---

## Prerequisites & Installation

### System Requirements
* **Java Development Kit (JDK)**: JDK must be installed and added to your `PATH`.
* **Build System**:
  * **Linux/macOS**: Standard `make` (build-essential package).
  * **Windows**: `mingw32-make` or similar MSYS2/MinGW toolchain.

### Library Dependencies
The project dependencies (LWJGL 3, JOML, GLFW, STB, OpenGL, and their respective natives) are needed to be configured in your system. Follow the steps in the **Downloading & Configuring Dependencies** section to do so.

<br>

> [!NOTE]
> **Downloading & Configuring Dependencies:**
>
> * **Linux / macOS:**
>   Run the provided shell script to automatically fetch and extract the required native libraries and JARs:
>   ```bash
>   chmod +x download_deps.sh
>   ./download_deps.sh
>   ```
>
> * **Windows:**
>   If you cannot run the bash script (e.g., when using standard Command Prompt or PowerShell), you can set up the dependencies manually:
>   1. Go to the [LWJGL 3 Customize Page](https://www.lwjgl.org/customize).
>   2. Click the **"Load Config"** button.
>   3. Select the config file: `lib/lwjgl-release-custom-zip.json` from the this project.
>   4. Download the generated ZIP bundle.
>   5. Move all main `.jar` libraries (like `lwjgl.jar`, `joml-1.10.8.jar`, etc.) directly into the `lib/` directory.
>   6. Extract the Windows `.dll` native libraries (such as `lwjgl.dll`, `lwjgl_opengl.dll`, `glfw.dll`, `lwjgl_stb.dll`) and place them in the `lib/natives/windows/x64/` directory of the project.

---

## Running the Game

Follow the quick steps below to compile and play **Neon Drift** on your system:

### Linux & macOS
1. **Clone the repository or download the ZIP** (and extract the files):
   ```bash
   git clone https://github.com/lvdejesus/cmsc137-game.git
   ```
2. **Build the source files**:
   ```bash
   make build
   ```
3. **Run the game client**:
   ```bash
   make run
   ```

### Windows
Using your terminal (CMD, PowerShell, or Git Bash with MinGW):
1. **Clone the repository or download the ZIP** (and extract the files):
   ```bash
   git clone https://github.com/lvdejesus/cmsc137-game.git
   ```
2. **Build the source files**:
   ```bash
   mingw32-make -f Makefile.win build
   ```
2. **Run the game client**:
   ```bash
   mingw32-make -f Makefile.win run
   ```

---

## Game Controls
* **Movement**: `W` / `A` / `S` / `D` or **Arrow Keys**
* **Aim & Shoot**: **Mouse Cursor** to aim, **Left Click** to fire bullets
* **Pause / Menu**: Press `Escape` to toggle the in-game menu
* **Upgrades**: Left-click on any **Upgrade Card** overlay when your player levels up

