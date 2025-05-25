package core.design.observer;

import core.controller.PlaneController;
import core.responses.Response;
import core.model.Plane;
import java.util.ArrayList;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

public class PlaneComboBoxObserver extends Observer {

    private JComboBox comboBox1;

    public PlaneComboBoxObserver(JComboBox comboBox1) {
        this.comboBox1 = comboBox1;
        initializeComboBox();
    }

    @Override
    public void notify(int value) {
        comboBox1.removeAllItems();
        initializeComboBox();

        Response response = PlaneController.getAllPlanes();
        handleResponse(response, comboBox1);
    }

    private void initializeComboBox() {
        comboBox1.addItem("Plane");
    }

    private void handleResponse(Response response, JComboBox comboBox) {
        if (response.getStatus() >= 500) {
            JOptionPane.showMessageDialog(null, response.getMessage(), "Error " + response.getStatus(), JOptionPane.ERROR_MESSAGE);
        } else if (response.getStatus() >= 400) {
            JOptionPane.showMessageDialog(null, response.getMessage(), "Error " + response.getStatus(), JOptionPane.WARNING_MESSAGE);
        } else {
            if (response.getObject() instanceof ArrayList) {
                for (Plane p : (ArrayList<Plane>) response.getObject()) {
                    comboBox.addItem(p.getId());
                }
            }
        }
    }
}