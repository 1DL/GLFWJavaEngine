package dl;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import renderer.DebugDraw;
import scenes.LevelEditorScene;
import scenes.LevelScene;
import scenes.Scene;
import util.MonitorHandler;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    private static final boolean CAPPED = true;
    private static final boolean UNCAPPED = false;

    private double renderFpsCap = 1.0 / 60;
    private double updateHzCap = 1.0 / 60;
    private boolean isRenderingCapped = UNCAPPED;
    private boolean isUpdatingCapped = UNCAPPED;

    private int width, height;
    private String title;
    private long glfwWindow;
    private ImGuiLayer imguiLayer;

    //Monitor related
    private boolean isFullscreen = false;
    private int[] windowedXSize = {0};
    private int[] windowedYSize = {0};
    private int[] windowedXPos = {0};
    private int[] windowedYPos = {0};
    private MonitorHandler monitorHandler;


    public float r, g, b, a;
    private boolean fadeToBlack = false;

    private static Window window = null;

    private static Scene currentScene;

    private Window() {
        this.width = 1920;
        this.height = 1080;
        this.title = "Mario - DL Engine";
        this.r = 1;
        this.g = 1;
        this.b = 1;
        this.a = 1;
    }

    public static void changeScene (int newScene) {
        switch (newScene) {
            case 0:
                currentScene = new LevelEditorScene();
                break;
            case 1:
                currentScene = new LevelScene();
                break;
            default:
                assert false : "Unknown scene '" + newScene +"'";
                break;
        }

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
        glfwSwapInterval(0);

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
        this.imguiLayer = new ImGuiLayer(glfwWindow);
        this.imguiLayer.initImGui();

        Window.changeScene(0);
        setFullscreen(true);
    }

    int count = 0;
    public void loop() {
        double lastUpdateTime = 0;  // number of seconds since the last loop
        double lastFrameTime = 0;   // number of seconds since the last frame


        while(!glfwWindowShouldClose(glfwWindow)) {

            double now = glfwGetTime();
            double deltaTime = now - lastUpdateTime;

            if (isUpdatingCapped) {
                if ((now - lastUpdateTime) >= updateHzCap) {
                    update(deltaTime);
                    lastUpdateTime = now;
                }
            } else {
                update(deltaTime);
                lastUpdateTime = now;
            }

            if (isRenderingCapped) {
                if ((now - lastFrameTime) >= renderFpsCap) {
                    render(deltaTime);
                    lastFrameTime = now;
                }
            } else {
                render(deltaTime);
                lastFrameTime = now;
            }
        }

        currentScene.saveExit();
    }

    private void update(double dt){
        //Poll events
        glfwPollEvents();

        //            if (KeyListener.isKeyPressed(GLFW_KEY_B)) {
//                System.out.println("hey teste");
//                //JoystickListener.isButtonPressed(0, GLFW_JOYSTICK_2);
//            }
//
//            if (KeyListener.isKeyPressed(GLFW_KEY_J)) {
//                JoystickListener.getAxis(0, GLFW_JOYSTICK_2);
//            }


        currentScene.update((float) dt);

    }

    private void render(double dt) {
        glClearColor(r, g, b, a);
        glClear(GL_COLOR_BUFFER_BIT);

        currentScene.render();
        this.imguiLayer.update((float) dt, currentScene);
        DebugDraw.beginFrame();
        DebugDraw.draw();
        glfwSwapBuffers(glfwWindow);
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
}
