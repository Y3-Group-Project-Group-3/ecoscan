
package com.example.monitortest.ui.livemonitor.components.icons;

import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import java.awt.Color;
import java.awt.Graphics2D;

// Handles drawing comparison icons for emissions milestones.
public class EmissionIconRenderer {

    // Draws the appropriate icon for a milestone.
    public static void drawIcon( Graphics2D g2, String milestoneName, int centreX, int centreY) {

        // Select an icon based on the milestone's name.
        FontAwesomeSolid iconCode;


        switch (milestoneName) {

            // Online activities
            case "Google Search":
                iconCode = FontAwesomeSolid.SEARCH;
                break;

            // Drinks and food
            case "Disposable Coffee Cup":
            case "Plant-Based Latte":
            case "Dairy Latte":
            case "Cup of Tea":
                iconCode = FontAwesomeSolid.COFFEE;
                break;

            case "Banana":
            case "Pasta":
            case "Beef":
                iconCode = FontAwesomeSolid.UTENSILS;
                break;

            case "Plastic Water Bottle":
                iconCode = FontAwesomeSolid.WINE_BOTTLE;
                break;

            // Household items and appliances
            case "Eco-Friendly Light Bulb":
            case "Standard Light Bulb":
                iconCode = FontAwesomeSolid.LIGHTBULB;
                break;

            case "Plastic Bag":
                iconCode = FontAwesomeSolid.SHOPPING_BAG;
                break;

            case "Water Kettle":
                iconCode = FontAwesomeSolid.MUG_HOT;
                break;

            case "Washing Machine":
            case "Tumble Dryer":
                iconCode = FontAwesomeSolid.TSHIRT;
                break;

            case "Microwave":
            case "Electric Oven":
                iconCode = FontAwesomeSolid.FIRE;
                break;

            case "Hot Shower":
                iconCode = FontAwesomeSolid.SHOWER;
                break;

            // Transport
            case "Public Transport":
                iconCode = FontAwesomeSolid.BUS;
                break;

            case "Car Commute":
                iconCode = FontAwesomeSolid.CAR;
                break;

            case "London to Paris Flight":
            case "NY to London Flight":
                iconCode = FontAwesomeSolid.PLANE;
                break;

            default:
                return;
        }


        // Create a consistent icon appearance.
        FontIcon icon = FontIcon.of( iconCode, 20, new Color(100, 116, 139) );

        // Centre the icon at the supplied ruler coordinates.
        int x = centreX - icon.getIconWidth() / 2;
        int y = centreY - icon.getIconHeight() / 2;

        // Paint the icon directly onto the comparison panel.
        icon.paintIcon(null, g2, x, y);
    }
}
