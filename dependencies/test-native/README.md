LWJGL 3.3.1 core/STB/GLFW/OpenGL and Windows x64 native jars from the local Minecraft 1.20.1
libraries, used by `tartarusNativeModelTest` for `NativeImage` allocation and the
offline RenderTower authoring preview (a hidden GLFW/OpenGL context).
They are not included in the mod JAR or installed into the mod profile. Other
operating systems require their corresponding LWJGL native artifact. These
libraries use the BSD license included in each JAR. No Minecraft window is opened.
