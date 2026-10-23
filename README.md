# Bird Client — Fabric 1.21.11

Purple-themed client-side Fabric mod. Press **Right Shift** to open or close the GUI.

## Included modules
- Entity ESP: glowing outlines on loaded entities.
- HUD: Bird Client label.
- Coordinates: live XYZ display.
- Trigger Assist: attacks the targeted living entity when the attack cooldown is ready; single-player only.
- Sprint Assist: enables sprint while moving forward.

## Build with GitHub Actions
1. Extract this ZIP.
2. Upload the **contents** of this folder to the root of your GitHub repository (not an extra nested `Bird-Client` folder).
3. Open **Actions** and run **Build Bird Client**, or push to `main`.
4. Open the completed workflow run and download the `Bird-Client-JAR` artifact.
5. Put the built `.jar` in your Minecraft Java Edition `mods` folder with Fabric Loader and Fabric API for Minecraft 1.21.11.

The workflow creates the JAR from source. This ZIP is a source project, not a precompiled JAR.

## Version target
Minecraft 1.21.11, Java 21, Fabric Loader 0.18.4, Fabric API 0.141.1+1.21.11, Fabric Loom 1.14.10.

## Compatibility note
The project has not been compiled in this environment. If GitHub Actions fails, send the first red error block from the build log so the specific API or dependency issue can be fixed.
