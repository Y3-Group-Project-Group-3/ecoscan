package com.example.monitortest.ui.loading;


import javax.swing.*;
import java.awt.*;

public class LoadingPanel extends JPanel {

    public LoadingPanel() {
        setLayout(new GridBagLayout());

        JLabel loadingLabel = new JLabel(
                "Scanning hardware..."
        );

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(
                new BoxLayout(contentPanel, BoxLayout.Y_AXIS)
        );

        loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(loadingLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(progressBar);

        add(contentPanel);
    }
}
