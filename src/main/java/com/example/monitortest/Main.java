package com.example.monitortest;

import com.example.monitortest.views.CreateWindow;
import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {


    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            CreateWindow window = new CreateWindow();
            window.createAndShowGUI();
        });
    }
}