package core.design.observer;

import core.controller.PassengerController;
import core.responses.Response;
import core.model.Passenger;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class PassengerFlightTableObserver extends Observer {

    private DefaultTableModel tableModel;
    private Passenger currentUser = null;

    public static final int CURRENT_USER_UPDATED = 3;

    public PassengerFlightTableObserver(DefaultTableModel tableModel) {
        this.tableModel = tableModel;
    }

    @Override
    public void notify(int value) {
        if (value == CURRENT_USER_UPDATED) {
            this.currentUser = UserManager.getInstance().getCurrentUser();
        }
        updatePassengerFlightsTable();
    }

    private void updatePassengerFlightsTable() {
        tableModel.setRowCount(0);

        if (this.currentUser == null) {
            return;
        }

        Response response = PassengerController.showPassengerFlights(String.valueOf(this.currentUser.getId()));
        handleResponse(response, tableModel);
    }

    private void handleResponse(Response response, DefaultTableModel model) {
        if (response.getStatus() >= 500) {
            JOptionPane.showMessageDialog(null, response.getMessage(), "Error " + response.getStatus(), JOptionPane.ERROR_MESSAGE);
        } else if (response.getStatus() >= 400) {
            JOptionPane.showMessageDialog(null, response.getMessage(), "Error " + response.getStatus(), JOptionPane.WARNING_MESSAGE);
        } else {
            if (response.getObject() instanceof ArrayList) {
                for (String[] data : (ArrayList<String[]>) response.getObject()) {
                    model.addRow(data);
                }
            }
        }
    }
}