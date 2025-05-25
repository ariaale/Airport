package core.design.observer;

import core.controller.PassengerController;
import core.responses.Response;
import core.model.Passenger;
import java.util.ArrayList;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

public class PassengerComboBoxObserver extends Observer {

    private JComboBox comboBox1;

    public static final int PASSENGER_ADDED = 1;

    public PassengerComboBoxObserver(JComboBox comboBox1) {
        this.comboBox1 = comboBox1;
        initializeComboBox();
    }

    @Override
    public void notify(int value) {
        if (value == PASSENGER_ADDED) {
            comboBox1.removeAllItems();
            initializeComboBox();

            Response response = PassengerController.getAllPassengers();
            handleResponse(response, comboBox1);
        }
    }

    private void initializeComboBox() {
        comboBox1.addItem("Select User");
    }

    private void handleResponse(Response response, JComboBox comboBox) {
        if (response.getStatus() >= 500) {
            JOptionPane.showMessageDialog(null, response.getMessage(), "Error " + response.getStatus(), JOptionPane.ERROR_MESSAGE);
        } else if (response.getStatus() >= 400) {
            JOptionPane.showMessageDialog(null, response.getMessage(), "Error " + response.getStatus(), JOptionPane.WARNING_MESSAGE);
        } else {
            if (response.getObject() instanceof ArrayList) {
                for (Passenger p : (ArrayList<Passenger>) response.getObject()) {
                    comboBox.addItem(String.valueOf(p.getId()));
                }
            }
        }
    }
}