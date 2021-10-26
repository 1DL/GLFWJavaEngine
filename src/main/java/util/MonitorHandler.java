package util;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVidMode;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;

public class MonitorHandler {

    private long glfwMonitor;
    private String activeMonitorName;
    private int activeMonitorIndex;

    private GLFWVidMode currentVideoMode;

    public  ArrayList<Long> availableMonitors = new ArrayList<>();
    //GLFWVidMode.Buffer videoModes;

    public MonitorHandler() {

    }

    /**
     * Sets the primary monitor as the active monitor, update the current video mode and update the list of available
     * monitors.
     */
    public void initPrimary() {
        glfwMonitor = glfwGetPrimaryMonitor();
        updateVideoMode();
        updateAvailableMonitors();
    }

    /**
     * Sets the active monitor by a custom index. Use this function to set the active monitor to be a different one from
     * the primary monitors. If the monitor index is found, sets the current video mode and update the list of available
     * monitors.
     * @param monitorIndex
     */
    public void initCustom(int monitorIndex) {
        updateAvailableMonitors();
        setActiveMonitor(monitorIndex);
    }

    /**
     * Clears the current list and Updates with current glfwMonitors detected.
     */
    public void updateAvailableMonitors() {
        PointerBuffer allMonitors = glfwGetMonitors();
        int monitorIndex = 0;
        availableMonitors.clear();
        System.out.println("Detecting Available Monitors");
        while (allMonitors.hasRemaining()) {
            long monitor = allMonitors.get();
            availableMonitors.add(monitor);
            System.out.println("Monitor index#" + monitorIndex + " - ID: " + monitor);
            monitorIndex++;
        }
    }

    /**
     * Sets the active glfwMonitor.
     * @param monitorIndex
     */
    public void setActiveMonitor(int monitorIndex) {
        try {
            glfwMonitor = availableMonitors.get(monitorIndex);
            System.out.println(glfwMonitor);
            updateVideoMode();
        } catch (IndexOutOfBoundsException ex) {
            assert false : "Error: Tried to set active monitor of index '" + monitorIndex + "' but it wasn't found on availableMonitors.";
        }
    }

    /**
     * Update the video mode of the active monitor.
     */
    public void updateVideoMode() {
        currentVideoMode = glfwGetVideoMode(glfwMonitor);
        activeMonitorName = glfwGetMonitorName(glfwMonitor);
        System.out.println("Active monitor properties: " + activeMonitorName);
        System.out.println("width: " + currentVideoMode.height());
        System.out.println("height: " + currentVideoMode.width());
        System.out.println("refresh rate: " + currentVideoMode.refreshRate());
    }

//    public void updateVideoModes() {
//        videoModes = glfwGetVideoModes(glfwMonitor);
//        int index = 0;
//        System.out.println("Getting videos mode of monitor active monitor");
//        while (videoModes.hasRemaining()) {
//            GLFWVidMode videoMode = videoModes.get();
//            //availableMonitors.add(videoMode);
//            System.out.println("mode index#" + index + " - value: " + videoMode);
//            index++;
//        }
//    }

    public long getGlfwMonitor() {
        return glfwMonitor;
    }

    public String getActiveMonitorName() {
        return activeMonitorName;
    }

    public int getActiveMonitorIndex() {
        return activeMonitorIndex;
    }

    public GLFWVidMode getCurrentVideoMode() {
        return currentVideoMode;
    }

    public ArrayList<Long> getAvailableMonitors() {
        return availableMonitors;
    }
}
