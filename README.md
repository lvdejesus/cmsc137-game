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
The project dependencies (LWJGL 3, JOML, GLFW, STB, OpenGL, and their respective natives) are pre-packaged in the repository.
* **Linux/macOS**: Library and native files are located in `lib/natives/x64/linux/`.
* **Windows**: Library and native files are located in `lib/natives/x64/windows/` or configured automatically.

<br>

> [!NOTE]
> **Downloading & Extracting Dependencies:**
> If you need to clean, re-download, or configure the LWJGL 3 libraries and native files on your machine, you can run the provided shell script:
> ```bash
> # 1. Make the script executable
> chmod +x download_deps.sh
> 
> # 2. Run the script to fetch libraries and extract native binaries
> ./download_deps.sh
> ```

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

