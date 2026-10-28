package com.example.monitortest.views.dialogs;


import javax.swing.*;
import java.awt.Component;

/**
 * Displays a confirmation dialog asking whether the user wants to restart EcoScan with administrator privileges.
 * This class only handles user interaction. It does not perform the restart itself.
 */
public class AdministratorRestartDialog {


    /**
     * Asks the user whether EcoScan should restart
     * with administrator privileges.
     *
     * The dialog explains why elevation may be necessary
     * and allows the user to accept or decline.
     *
     * @param parent The Swing component that owns the dialog.
     * @return true if the user agrees, otherwise false.
     */
    public boolean confirmAdministratorRestart(Component parent) {

        /*
         * Explain why EcoScan is requesting elevation.
         *
         * This dialog should only appear when a hardware scan has failed and administrator permissions may help.
         */
        String message =
                "EcoScan could not access some hardware sensors.\n\n" +
                        "Administrator privileges may be required to retrieve " +
                        "CPU power information.\n\n" +
                        "Would you like to restart EcoScan as administrator?";

        /*
         * Display a confirmation dialog.
         *
         * YES_NO_OPTION provides two choices.
         * WARNING_MESSAGE indicates that this action will
         * change the application's permission level.
         */
        int response = JOptionPane.showConfirmDialog(
                parent,
                message,
                "Administrator Privileges Required",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        // Return true only when the user explicitly selects Yes.
        return response == JOptionPane.YES_OPTION;
    }


}
