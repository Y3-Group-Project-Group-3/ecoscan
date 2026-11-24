package com.example.monitortest.ui.livemonitor.components;

import com.example.monitortest.hardware.monitoring.LiveMonitorAvailability;

import com.example.monitortest.services.startup.ApplicationProcessManager;

import static com.example.monitortest.ui.theme.UIUtility.font;

import javax.swing.*;
import java.awt.*;

import static com.example.monitortest.ui.theme.AppTheme.*;

// Displays an explanation when live hardware monitoring
// cannot operate in the current environment.
public class LiveMonitorUnavailablePanel extends JPanel {

    // Callback supplied by the main application when administrator
    // privileges are needed to access hardware sensors.
    private Runnable administratorRestartAction;

    // Creates the unavailable-state panel.
    public LiveMonitorUnavailablePanel() {

        // Centre the unavailable-state message within the panel.
        super(new GridBagLayout());

        // Match the existing Live Monitor background.
        setBackground(BG_COLOR);
    }

    // Configures the action used to restart EcoScan as administrator.
    public void setAdministratorRestartAction(Runnable action) {
        this.administratorRestartAction = action;
    }


    // Displays the appropriate unavailable-state message.
    // Includes administrator restart options when required.
    public void showStatus(LiveMonitorAvailability.Status status) {

        // Clear existing content to avoid duplicate messages.
        removeAll();

        String title;
        String description;

        // Select the message for the current environment.
        switch (status) {

            case WINDOWS_ONLY -> {
                title = "Windows Only";
                description = "Live hardware monitoring is currently supported on Windows only.";
            }

            case ADMIN_REQUIRED -> {
                title = "Administrator Privileges Required";
                description = "Live hardware monitoring requires administrator privileges to access hardware sensors.";
            }

            default -> {
                return;
            }
        }

        // Arrange the message elements vertically.
        JPanel messageContent = new JPanel();
        messageContent.setLayout( new BoxLayout(messageContent, BoxLayout.Y_AXIS) );
        messageContent.setOpaque(false);

        // Display the unavailable-state heading.
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(font(Font.BOLD, 22));
        titleLabel.setForeground(TEXT_MAIN);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Explain why monitoring is unavailable.
        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(font(Font.PLAIN, 14));
        descriptionLabel.setForeground(TEXT_MUTED);
        descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        messageContent.add(titleLabel);
        messageContent.add(Box.createVerticalStrut(12));
        messageContent.add(descriptionLabel);

        // Display administrator restart options only when necessary.
        if (status == LiveMonitorAvailability.Status.ADMIN_REQUIRED) {

            ApplicationProcessManager processManager = new ApplicationProcessManager();

            messageContent.add(Box.createVerticalStrut(20));

            if (processManager.isPackagedApplication()) {

                // Packaged applications can request Windows UAC elevation.
                JButton restartButton = new JButton("Restart as Administrator");

                restartButton.setFont(font(Font.BOLD, 13));
                restartButton.setBackground(new Color(22, 137, 76));
                restartButton.setForeground(Color.WHITE);
                restartButton.setFocusPainted(false);
                restartButton.setCursor( Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) );
                restartButton.setAlignmentX(Component.CENTER_ALIGNMENT);

                // Delegate the restart operation to the main application.
                restartButton.addActionListener(event -> {
                    if (administratorRestartAction != null) {
                        administratorRestartAction.run();
                    }
                });

                messageContent.add(restartButton);

            } else {

                // IntelliJ launches require manually restarting the IDE.
                JLabel instructionLabel = new JLabel("Restart IDE as administrator to enable live monitoring.");

                instructionLabel.setFont(font(Font.PLAIN, 13));
                instructionLabel.setForeground(TEXT_MUTED);
                instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                messageContent.add(instructionLabel);
            }
        }

        // Centre the completed message within this panel.
        add(messageContent);

        // Refresh the panel after its content changes.
        revalidate();
        repaint();
    }

}
