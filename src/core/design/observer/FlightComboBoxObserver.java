package core.design.observer;

import core.controller.FlightController;
import core.responses.Response;
import core.model.Flight;
import java.util.ArrayList;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

public class FlightComboBoxObserver extends Observer {

    private JComboBox comboBox1;
    private JComboBox comboBox2;

    public static final int FLIGHT_ADDED = 1;

    public FlightComboBoxObserver(JComboBox comboBox1, JComboBox comboBox2) {
        this.comboBox1 = comboBox1;
        this.comboBox2 = comboBox2;
        initializeComboBoxes();
    }

    @Override
    public void notify(int value) {
        if (value == FLIGHT_ADDED) {
            comboBox1.removeAllItems();
            comboBox2.removeAllItems();
            initializeComboBoxes();

            Response response = FlightController.getAllFlights();
            handleResponse(response);
        }
    }

    private void initializeComboBoxes() {
        comboBox1.addItem("Flight");
        comboBox2.addItem("ID");
    }

    private void handleResponse(Response response) {
        if (response.getStatus() >= 500) {
            JOptionPane.showMessageDialog(null, response.getMessage(), "Error " + response.getStatus(), JOptionPane.ERROR_MESSAGE);
        } else if (response.getStatus() >= 400) {
            JOptionPane.showMessageDialog(null, response.getMessage(), "Error " + response.getStatus(), JOptionPane.WARNING_MESSAGE);
        } else {
            for (Flight f : (ArrayList<Flight>) response.getObject()) {
                comboBox1.addItem(f.getId());
                comboBox2.addItem(f.getId());
            }
        }
    }
}