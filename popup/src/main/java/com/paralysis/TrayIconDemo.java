package com.paralysis;

import java.awt.*;
import java.awt.TrayIcon.MessageType;

public class TrayIconDemo {

    public static void main(String[] args) throws AWTException, java.net.MalformedURLException {
        if (SystemTray.isSupported()) {
            TrayIconDemo td = new TrayIconDemo();
            td.displayTray();
        } else {
            System.err.println("System tray not supported!");
        }
    }

    private void displayTray() throws AWTException, java.net.MalformedURLException {
        //Obtain only one instance of the SystemTray object
        SystemTray tray = SystemTray.getSystemTray();

        //If the icon is a file
        Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
        //Alternative (if the icon is on the classpath):
        //Image image = Toolkit.getToolkit().createImage(getClass().getResource("icon.png"));
        TrayIcon trayIcon = new TrayIcon(image, "Tray Demo");
        //Let the system resizes the image if needed
        trayIcon.setImageAutoSize(true);
        //Set tooltip text for the tray icon
        trayIcon.setToolTip("System tray icon demo");
        tray.add(trayIcon);
        System.out.println("???");
        for(TrayIcon t: tray.getTrayIcons()) {
            System.out.println(t.getActionCommand());
            System.out.println(t.getToolTip());
        }
        trayIcon.displayMessage("Hello, World", "notification demo", MessageType.INFO);
    }
}
