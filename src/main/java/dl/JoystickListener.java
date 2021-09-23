package dl;

import static org.lwjgl.glfw.GLFW.*;

public class JoystickListener {

    private static JoystickListener instance;
    private boolean joysticksConnected[] = new boolean[16];

    private JoystickListener() {

    }

    public static JoystickListener get() {
        if (JoystickListener.instance == null) {
            JoystickListener.instance = new JoystickListener();
        }

        return JoystickListener.instance;
    }

    public static void joystickCallback(int joystickId, int event) {
        if (event == GLFW_CONNECTED)
        {
            get().joysticksConnected[joystickId] = true;
        }
        else if (event == GLFW_DISCONNECTED)
        {
            get().joysticksConnected[joystickId] = false;
        }
    }
}
