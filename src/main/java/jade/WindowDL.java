
/*
package jade;

import observers.EventSystem;
import observers.Observer;
import observers.events.Event;
import observers.events.EventType;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import renderer.*;
import scenes.LevelEditorSceneInitializer;
import scenes.Scene;
import scenes.SceneInitializer;
import util.AssetPool;
import util.MonitorHandler;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window implements Observer {

    private static final boolean WINDOWED = false;
    private static final boolean FULLSCREEN = true;
    private static final boolean CAPPED = true;
    private static final boolean UNCAPPED = false;
    private static final int  VSYNC_OFF = 0;
    private static final int VSYNC_ON = 1;
    private static final int VSYNC_ON_DOUBLE_BUFFER = 2;
    private static final int VSYNC_ON_TRIPLE_BUFFER = 3;

    private double renderFpsCap = 1.0 / 60;
    private boolean isRenderingCapped = CAPPED;
    private boolean isFullscreen = FULLSCREEN;
    private int swapInterval = VSYNC_OFF;

    //Monitor related
    private int[] windowedXSize = {0};
    private int[] windowedYSize = {0};
    private int[] windowedXPos = {0};
    private int[] windowedYPos = {0};
    private MonitorHandler monitorHandler;


    private int width, height;
    private String title;
    private long glfwWindow;
    private ImGuiLayer imguiLayer;
    private Framebuffer framebuffer;
    private PickingTexture pickingTexture;
    private Shader defaultShader;
    private Shader pickingShader;
    private boolean runtimePlaying = false;

    private static Window window = null;

    private static Scene currentScene;



    private Window() {
        this.width = 1920;
        this.height = 1080;
        this.title = "Mario - DL Engine";
        EventSystem.addObserver(this);
    }

    public static void changeScene (SceneInitializer sceneInitializer) {
        if (currentScene != null) {
            currentScene.destroy();
        }

        getImguiLayer().getPropertiesWindow().setActiveGameObject(null);
        currentScene = new Scene(sceneInitializer);
        currentScene.load();
        currentScene.init();
        currentScene.start();
    }

    public static Window get() {
        if (Window.window == null) {
            Window.window = new Window();
        }

        return Window.window;
    }

    public static Scene getScene() {
        return get().currentScene;
    }

    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        init();
        loop();

        // Free the memory
        glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);

        // Terminate GLFW and the free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public void init() {
        // Setup an error callback
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW.");
        }

        // Configure GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);

        //Configure monitors
        monitorHandler = new MonitorHandler();
        //Set primary monitor
        monitorHandler.initPrimary();

        //Create the Window
//        glfwWindow = glfwCreateWindow(monitorHandler.getCurrentVideoMode().width(),
//                monitorHandler.getCurrentVideoMode().height(), this.title, monitorHandler.getGlfwMonitor(), NULL);

        glfwWindow = glfwCreateWindow(this.width, this.height, this.title, NULL, NULL);


        if (glfwWindow == NULL){
            throw new IllegalStateException("Failed to create the GLFW window.");
        }

        //Setting Mouse and Keyboard listeners callback
        //Mouse
        glfwSetCursorPosCallback(glfwWindow, MouseListener::mousePosCallback);
        glfwSetMouseButtonCallback(glfwWindow, MouseListener::mouseButtonCallback);
        glfwSetScrollCallback(glfwWindow, MouseListener::mouseScrollCallback);
        //Keyboard
        glfwSetKeyCallback(glfwWindow, KeyListener::keyCallback);
        //Joystick
        glfwSetJoystickCallback(JoystickListener::joystickCallback);
        //Screen resize callback
        glfwSetWindowSizeCallback(glfwWindow, (w, newWidth, newHeight) -> {
            Window.setWidth(newWidth);
            Window.setHeight(newHeight);
        });

        // Make the OpenGL context current
        glfwMakeContextCurrent(glfwWindow);
        // Enable v-sync
        glfwSwapInterval(swapInterval);

        // Make the window visible
        glfwShowWindow(glfwWindow);

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);


        this.framebuffer = new Framebuffer(1920, 1080);
        this.pickingTexture = new PickingTexture(1920, 1080);
        glViewport(0,0, 1920, 1080);

        this.imguiLayer = new ImGuiLayer(glfwWindow, pickingTexture);
        this.imguiLayer.initImGui();

        Window.changeScene(new LevelEditorSceneInitializer());
        setFullscreen(isFullscreen);
    }

    double start_time = System.nanoTime();

    int fpsCounter = 0;

    public void loop() {
        double lastUpdateTime = 0;  // number of seconds since the last update

        Shader defaultShader = AssetPool.getShader("assets/shaders/default.glsl");
        Shader pickingShader = AssetPool.getShader("assets/shaders/pickingShader.glsl");

        while(!glfwWindowShouldClose(glfwWindow)) {
            double now = glfwGetTime();
            double deltaTime = (now - lastUpdateTime);

            // Poll events
            glfwPollEvents();


            if (isRenderingCapped) {
                if ((now - lastUpdateTime) >= renderFpsCap) {

                    // Render pass 1. Render to picking texture
                    glDisable(GL_BLEND);
                    pickingTexture.enableWriting();

                    glViewport(0, 0, 1920, 1080);
                    glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
                    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

                    Renderer.bindShader(pickingShader);
                    currentScene.render();

                    pickingTexture.disableWriting();
                    glEnable(GL_BLEND);

                    // Render pass 2. Render actual game
                    DebugDraw.beginFrame();

                    this.framebuffer.bind();
                    glClearColor(1, 1, 1, 1);
                    glClear(GL_COLOR_BUFFER_BIT);

                    DebugDraw.draw();
                    Renderer.bindShader(defaultShader);
                    if (runtimePlaying) {
                        currentScene.update((float) deltaTime);
                    } else {
                        currentScene.editorUpdate((float) deltaTime);
                    }
                    currentScene.render();

                    this.framebuffer.unbind();

                    this.imguiLayer.update((float) deltaTime, currentScene);
                    glfwSwapBuffers(glfwWindow);
                    MouseListener.endFrame();

                    lastUpdateTime = now;
                }
            } else {

                // Render pass 1. Render to picking texture
                glDisable(GL_BLEND);
                pickingTexture.enableWriting();

                glViewport(0, 0, 1920, 1080);
                glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

                Renderer.bindShader(pickingShader);
                currentScene.render();

                pickingTexture.disableWriting();
                glEnable(GL_BLEND);

                // Render pass 2. Render actual game
                DebugDraw.beginFrame();

                this.framebuffer.bind();
                glClearColor(1,1,1,1);
                glClear(GL_COLOR_BUFFER_BIT);

                DebugDraw.draw();
                Renderer.bindShader(defaultShader);
                currentScene.update((float) deltaTime);
                currentScene.render();

                this.framebuffer.unbind();

                this.imguiLayer.update((float) deltaTime, currentScene);
                glfwSwapBuffers(glfwWindow);
                MouseListener.endFrame();

                lastUpdateTime = now;
            }
        }
    }


    public static int getWidth() {
        return get().width;
    }

    public static int getHeight() {
        return get().height;
    }

    public static void setWidth(int newWidth) {
        get().width = newWidth;
    }

    public static void setHeight(int newHeight) {
        get().height = newHeight;
    }

    public boolean isFullscreen() {
        return glfwGetWindowMonitor(glfwWindow) != NULL;
    }

    public void setFullscreen(boolean fullscreen){
        if (isFullscreen() == fullscreen) {
            return;
        }
        if (fullscreen) {
            // backup window position and window size
            glfwGetWindowPos(glfwWindow, windowedXPos, windowedYPos);
            glfwGetWindowSize(glfwWindow, windowedXSize, windowedYSize);

            // get resolution of monitor

            // switch to full screen
            try {
                glfwSetWindowMonitor(glfwWindow, monitorHandler.getGlfwMonitor(), 0, 0,
                        monitorHandler.getCurrentVideoMode().width(), monitorHandler.getCurrentVideoMode().width(),
                        monitorHandler.getCurrentVideoMode().refreshRate());
            } catch (Exception ex) {
                assert false : "Error: Failed to get inside fullscreen.";
            }
            isFullscreen = true;
        } else {
            // restore last window size and position
            try {
                glfwSetWindowMonitor(glfwWindow, NULL,  windowedXPos[0], windowedYPos[0], windowedXSize[0], windowedYSize[0], 0 );
            } catch (Exception ex) {
                assert false : "Error: Failed to exit from fullscreen.";
            }
            isFullscreen = false;
        }
    }

    public double getRenderFpsCap() {
        return renderFpsCap;
    }

    public void setRenderFpsCap(int targetFps) {
        this.renderFpsCap = 1.0 / targetFps;;
    }


    public boolean isRenderingCapped() {
        return isRenderingCapped;
    }

    public void setRenderingCapped(boolean renderingCapped) {
        isRenderingCapped = renderingCapped;
    }

    public static Framebuffer getFramebuffer() {
        return get().framebuffer;
    }

    public static float getTargetAspectRatio() {
        return 16.0f / 9.0f;
    }

    public static ImGuiLayer getImguiLayer() {
        return get().imguiLayer;
    }

    @Override
    public void onNotify(GameObject object, Event event) {
        switch (event.type) {
            case GameEngineStartPlay :
                this.runtimePlaying = true;
                currentScene.save();
                Window.changeScene(new LevelEditorSceneInitializer());
                break;
            case GameEngineStopPlay:
                this.runtimePlaying = false;
                Window.changeScene(new LevelEditorSceneInitializer());
                break;
            case LoadLevel:
                Window.changeScene(new LevelEditorSceneInitializer());
                break;
            case SaveLevel:
                currentScene.save();
                break;
            case UserEvent:
                break;
        }
    }
}
*/