package core.design.observer;

import core.controller.PassengerController;
import core.responses.Response;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class PassengerTableObserver extends Observer {

    private DefaultTableModel tableModel;

    public PassengerTableObserver(DefaultTableModel tableModel) {
        this.tableModel = tableModel;
    }

    @Override
    public void notify(int value) {
        updatePassengerTable();
    }

    private void updatePassengerTable() {
        tableModel.setRowCount(0);
        Response response = PassengerController.getPassengersWithFormat();
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