package com.example.monitortest.views;

import com.example.monitortest.model.HardwareInfo;

import javax.swing.*;
import java.awt.*;

import static java.awt.AWTEventMulticaster.add;

public class HomePanel extends JPanel {

    private final JLabel mainText;

    private final JButton viewScanPageButton;

    public HomePanel() {

        mainText = new JLabel("...");
        add(mainText);

        // Go To Scan Page
        viewScanPageButton = new JButton("Go To Scan Page");
        // Go to History Page


        mainText.setText("Welcome to EcoScan :-D");

        viewScanPageButton.setAlignmentX(Component.CENTER_ALIGNMENT);


        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(
                new BoxLayout(contentPanel, BoxLayout.Y_AXIS)
        );

        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(mainText);
        contentPanel.add(Box.createVerticalStrut(25));
        contentPanel.add(viewScanPageButton);

        add(contentPanel);
    }

    public JButton getScanButton() {
        return viewScanPageButton;
    }


}
