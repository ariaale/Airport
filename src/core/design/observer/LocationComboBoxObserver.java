package core.design.observer;

import core.controller.LocationController;
import core.responses.Response;
import core.model.Location;
import java.util.ArrayList;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

public class LocationComboBoxObserver extends Observer {

    private JComboBox comboBox1;
    private JComboBox comboBox2;
    private JComboBox comboBox3;

    public LocationComboBoxObserver(JComboBox comboBox1, JComboBox comboBox2, JComboBox comboBox3) {
        this.comboBox1 = comboBox1;
        this.comboBox2 = comboBox2;
        this.comboBox3 = comboBox3;
        initializeComboBoxes();
    }

    @Override
    public void notify(int value) {
        comboBox1.removeAllItems();
        comboBox2.removeAllItems();
        comboBox3.removeAllItems();
        initializeComboBoxes();

        Response response = LocationController.getAllLocations();
        handleResponse(response);
    }

    private void initializeComboBoxes() {
        comboBox1.addItem("Location");
        comboBox2.addItem("Location");
        comboBox3.addItem("Location");
    }

    private void handleResponse(Response response) {
        if (response.getStatus() >= 500) {
            JOptionPane.showMessageDialog(null, response.getMessage(), "Error " + response.getStatus(), JOptionPane.ERROR_MESSAGE);
        } else if (response.getStatus() >= 400) {
            JOptionPane.showMessageDialog(null, response.getMessage(), "Error " + response.getStatus(), JOptionPane.WARNING_MESSAGE);
        } else {
            for (Location f : (ArrayList<Location>) response.getObject()) {
                comboBox1.addItem(f.getAirportId());
                comboBox2.addItem(f.getAirportId());
                comboBox3.addItem(f.getAirportId());
            }
        }
    }
}