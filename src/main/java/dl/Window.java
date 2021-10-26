package dl;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import util.MonitorHandler;

import java.util.ArrayList;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

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
        this.width = 1280;
        this.height = 720;
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
                currentScene.init();
                currentScene.start();
                break;
            case 1:
                currentScene = new LevelScene();
                currentScene.init();
                currentScene.start();
                break;
            default:
                assert false : "Unknown scene '" + newScene +"'";
                break;
        }
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

        //Free the memory
        glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);

        //Terminate GLFW and the free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public void init() {
        //Setup an error callback
        GLFWErrorCallback.createPrint(System.err).set();

        //Initialize GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        //Configure GLFW
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

        //setFullscreen(true);

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

        //Make the OpenGL context current
        glfwMakeContextCurrent(glfwWindow);
        //Enable v-sync, 1 for on
        glfwSwapInterval(0);

        //Make the window visible
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
    }

    int count = 0;
    public void loop() {
        float beginTime = (float)glfwGetTime();
        float endTime;
        float dt = -1.0f;

        while(!glfwWindowShouldClose(glfwWindow)) {
            //Poll events
            glfwPollEvents();

//            count++;
            //System.out.println(count);

            glClearColor(r, g, b, a);
            glClear(GL_COLOR_BUFFER_BIT);

//            if (count == 20000) {
//                setFullscreen(!isFullscreen);
//                count = 0;
//            }
////
//            if (KeyListener.isKeyPressed(GLFW_KEY_B)) {
//                System.out.println("hey teste");
//                //JoystickListener.isButtonPressed(0, GLFW_JOYSTICK_2);
//            }
//
//            if (KeyListener.isKeyPressed(GLFW_KEY_J)) {
//                JoystickListener.getAxis(0, GLFW_JOYSTICK_2);
//            }


            if (dt >= 0) {
                currentScene.update(dt);
            }

            this.imguiLayer.update(dt, currentScene);
            glfwSwapBuffers(glfwWindow);

            endTime = (float)glfwGetTime();
            dt = endTime - beginTime;
            beginTime = endTime;
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
}
