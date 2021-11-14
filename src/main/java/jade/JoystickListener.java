package jade;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.*;

public class JoystickListener {

    private static JoystickListener instance;
    private boolean joysticksConnected[] = new boolean[16];
    private boolean buttonPressed[] = new boolean[30];
    private float joystickAxes[] = new float[30];

    private JoystickListener() {

    }

    public static JoystickListener get() {
        if (JoystickListener.instance == null) {
            JoystickListener.instance = new JoystickListener();
            System.out.println(glfwGetJoystickName(GLFW_JOYSTICK_1));
            System.out.println(glfwGetJoystickName(GLFW_JOYSTICK_2));
            System.out.println(glfwGetJoystickName(GLFW_JOYSTICK_3));
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

    public static boolean isJoystickConnected(int joystickId) {
        for (int i = 0; i < get().joysticksConnected.length; i++) {
            System.out.println("Joy ID: " + i + ", connected: " + get().joysticksConnected[i]);
        }
        return get().joysticksConnected[joystickId];
    }

    public static void pollButtons(int joystickId) {
        ByteBuffer joyButtons = glfwGetJoystickButtons(joystickId);
        int buttonId = 0;

        while (joyButtons.hasRemaining()) {
            byte button = joyButtons.get();
            if (button == 1) {
                get().buttonPressed[buttonId] = true;
            } else {
                get().buttonPressed[buttonId] = false;
            }
            System.out.println("Btn ID: " + buttonId + " - " + get().buttonPressed[buttonId]);
            buttonId++;
        }

    }

    public static void pollAxis(int joystickId) {
        FloatBuffer joyAxis = glfwGetJoystickAxes(joystickId);
        int axisId = 0;

        while (joyAxis.hasRemaining()) {
            float axis = joyAxis.get();
            get().joystickAxes[axisId] =  round2(axis, 2);
            System.out.println("Axis ID: " + axisId + " - " + get().joystickAxes[axisId]);
            axisId++;
        }
    }

    public static boolean isButtonPressed(int buttonId, int joystickId) {
        if (!glfwJoystickPresent(joystickId)) return false;

        pollButtons(joystickId);

        return get().buttonPressed[buttonId];
    }

    public static float getAxis(int axisId, int joystickId) {
        if (!glfwJoystickPresent(joystickId)) return 0f;

        pollAxis(joystickId);

        return get().joystickAxes[axisId];
    }

    private static float round2(float number, int scale) {
        int pow = 10;
        for (int i = 1; i < scale; i++)
            pow *= 10;
        float tmp = number * pow;
        return ( (float) ( (int) ((tmp - (int) tmp) >= 0.5f ? tmp + 1 : tmp) ) ) / pow;
    }


}
