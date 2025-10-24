package Presentation.Views;

import Domain.Dtos.cars.CarResponseDto;
import Presentation.Models.CarsTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class CarsView {
    // Nombres de campo exactos requeridos por el .form
    private JPanel ContentPanel;
    private JPanel FormPanel;
    private JTable CarsTable;
    private JScrollPane CarsTableScroll;
    private JButton AgregarButton;
    private JButton BorrarButton;
    private JButton ClearButton;
    private JButton UpdateButton;
    private JTextField CarMakeField;
    private JTextField CarModelField;
    private JTextField YearTextField;
    private JButton AddMaintenance;
    private JButton ViewMaintenance;

    private final CarsTableModel tableModel;
    private final LoadingOverlay loadingOverlay;
    private final JFrame parentFrame; // para el overlay o posicionamiento


    public CarsView(JFrame parentFrame) {
        this.parentFrame = parentFrame;

        // Si el .form ya creó ContentPanel, usarlo; si no, crear una estructura por código (fallback).
        if (ContentPanel == null) {
            ContentPanel = new JPanel(new BorderLayout(8, 8));
            ContentPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        }

        tableModel = new CarsTableModel();
        if (CarsTable == null) {
            CarsTable = new JTable(tableModel);
            CarsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            CarsTable.setAutoCreateRowSorter(true);
        } else {
            CarsTable.setModel(tableModel);
        }

        if (CarsTableScroll == null) {
            CarsTableScroll = new JScrollPane(CarsTable);
        } else {
            CarsTableScroll.setViewportView(CarsTable);
        }

        if (FormPanel == null) {
            FormPanel = new JPanel();
            FormPanel.setLayout(new BoxLayout(FormPanel, BoxLayout.Y_AXIS));
            FormPanel.setPreferredSize(new Dimension(300, 0));
        }

        if (CarMakeField == null) CarMakeField = new JTextField();
        if (CarModelField == null) CarModelField = new JTextField();
        if (YearTextField == null) YearTextField = new JTextField();

        // Botones (crear si el diseñador no los creo)
        if (AgregarButton == null) AgregarButton = new JButton("Agregar");
        if (UpdateButton == null) UpdateButton = new JButton("Actualizar");
        if (BorrarButton == null) BorrarButton = new JButton("Borrar");
        if (ClearButton == null) ClearButton = new JButton("Limpiar");
        if (AddMaintenance == null) AddMaintenance = new JButton("Agregar mantenimiento");
        if (ViewMaintenance == null) ViewMaintenance = new JButton("Ver mantenimientos");

        if (ContentPanel.getComponentCount() == 0) {
            ContentPanel.add(CarsTableScroll, BorderLayout.CENTER);

            FormPanel.removeAll();
            FormPanel.add(new JLabel("Make:"));
            FormPanel.add(CarMakeField);
            FormPanel.add(Box.createVerticalStrut(8));
            FormPanel.add(new JLabel("Model:"));
            FormPanel.add(CarModelField);
            FormPanel.add(Box.createVerticalStrut(8));
            FormPanel.add(new JLabel("Year:"));
            FormPanel.add(YearTextField);
            FormPanel.add(Box.createVerticalStrut(12));

            JPanel buttons = new JPanel(new GridLayout(0, 1, 6, 6));
            buttons.add(AgregarButton);
            buttons.add(UpdateButton);
            buttons.add(BorrarButton);
            buttons.add(ClearButton);
            FormPanel.add(buttons);
            FormPanel.add(Box.createVerticalStrut(12));

            JPanel maintButtons = new JPanel(new GridLayout(0, 1, 6, 6));
            maintButtons.add(AddMaintenance);
            maintButtons.add(ViewMaintenance);
            FormPanel.add(maintButtons);

            ContentPanel.add(FormPanel, BorderLayout.EAST);
        } else {
            // Si el diseñador ya armó todo, asegurarse que la tabla use nuestro tableModel
            CarsTable.setModel(tableModel);
        }

        loadingOverlay = new LoadingOverlay(parentFrame);

    }

    // Getter que el .form y el Controller esperan
    public JPanel getContentPanel() { return ContentPanel; }

    public void showLoading(boolean visible) {
        loadingOverlay.show(visible);
    }

    public CarsTableModel getTableModel() { return tableModel; }
    public JTable getCarsTable() { return CarsTable; }

    public JTextField getCarMakeField() { return CarMakeField; }
    public JTextField getCarModelField() { return CarModelField; }
    public JTextField getYearTextField() { return YearTextField; }

    public JButton getAgregarButton() { return AgregarButton; }
    public JButton getBorrarButton() { return BorrarButton; }
    public JButton getClearButton() { return ClearButton; }
    public JButton getUpdateButton() { return UpdateButton; }
    public JButton getAddMaintenanceButton() { return AddMaintenance; }
    public JButton getViewMaintenanceButton() { return ViewMaintenance; }

    public void clearFields() {
        CarMakeField.setText("");
        CarModelField.setText("");
        YearTextField.setText("");
        CarsTable.clearSelection();
    }

    public void populateFields(CarResponseDto car) {
        if (car == null) return;
        CarMakeField.setText(car.getMake());
        CarModelField.setText(car.getModel());
        YearTextField.setText(String.valueOf(car.getYear()));
    }

    public void addAgregarListener(ActionListener l) { AgregarButton.addActionListener(l); }
    public void addUpdateListener(ActionListener l) { UpdateButton.addActionListener(l); }
    public void addBorrarListener(ActionListener l) { BorrarButton.addActionListener(l); }
    public void addClearListener(ActionListener l) { ClearButton.addActionListener(l); }
    public void addAddMaintenanceListener(ActionListener l) { AddMaintenance.addActionListener(l); }
    public void addViewMaintenanceListener(ActionListener l) { ViewMaintenance.addActionListener(l); }
}
