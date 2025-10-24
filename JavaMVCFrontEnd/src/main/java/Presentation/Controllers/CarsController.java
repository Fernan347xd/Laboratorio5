package Presentation.Controllers;

import Domain.Dtos.auth.UserResponseDto;
import Domain.Dtos.cars.AddCarRequestDto;
import Domain.Dtos.cars.CarResponseDto;
import Domain.Dtos.cars.DeleteCarRequestDto;
import Domain.Dtos.cars.UpdateCarRequestDto;
import Domain.Dtos.maintenance.AddMaintenanceRequestDto;
import Domain.Dtos.maintenance.MaintenanceResponseDto;
import Presentation.Observable;
import Presentation.Views.CarsView;
import Services.CarService;
import Services.MaintenanceService;
import Utilities.EventType;
import Utilities.MaintenanceType;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Future;

public class CarsController extends Observable {
    private final CarsView carsView;
    private final CarService carService;
    private final MaintenanceService maintenanceService;
    private final UserResponseDto user;

    public CarsController(CarsView carsView, CarService carService, MaintenanceService maintenanceService, UserResponseDto user) {
        this.carsView = carsView;
        this.carService = carService;
        this.maintenanceService = maintenanceService;
        this.user = user;
        addObserver(carsView.getTableModel());
        loadCarsAsync();
        addListeners();
    }

    private void loadCarsAsync() {
        carsView.showLoading(true);
        SwingWorker<List<CarResponseDto>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<CarResponseDto> doInBackground() throws Exception {
                Long userId = user != null ? user.getId() : null;
                return carService.listCarsAsync(userId).get();
            }
            @Override
            protected void done() {
                carsView.showLoading(false);
                try {
                    List<CarResponseDto> cars = get();
                    carsView.getTableModel().setCars(cars != null ? cars : new ArrayList<>());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    carsView.getTableModel().setCars(new ArrayList<>());
                }
            }
        };
        worker.execute();
    }

    private void addListeners() {
        carsView.getAgregarButton().addActionListener(e -> handleAddCar());
        carsView.getUpdateButton().addActionListener(e -> handleUpdateCar());
        carsView.getBorrarButton().addActionListener(e -> handleDeleteCar());
        carsView.getClearButton().addActionListener(e -> handleClearFields());
        carsView.getCarsTable().getSelectionModel().addListSelectionListener(this::handleRowSelection);
        carsView.getAddMaintenanceButton().addActionListener(e -> handleAddMaintenance());
        carsView.getViewMaintenanceButton().addActionListener(e -> handleViewMaintenance());
    }

    private void handleAddCar() {
        try {
            String make = carsView.getCarMakeField().getText();
            String model = carsView.getCarModelField().getText();
            int year = Integer.parseInt(carsView.getYearTextField().getText());

            Long userId = user != null ? user.getId() : null;
            AddCarRequestDto dto = new AddCarRequestDto(make, model, year, userId);

            SwingWorker<CarResponseDto, Void> worker = new SwingWorker<>() {
                @Override
                protected CarResponseDto doInBackground() throws Exception {
                    Future<CarResponseDto> f = carService.addCarAsync(dto, userId);
                    return f.get();
                }

                @Override
                protected void done() {
                    try {
                        CarResponseDto car = get();
                        if (car != null) {
                            notifyObservers(EventType.CREATED, car);
                            carsView.clearFields();
                            loadCarsAsync();
                        } else {
                            JOptionPane.showMessageDialog(carsView.getContentPanel(), "No se pudo agregar el vehículo.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(carsView.getContentPanel(), "Error al agregar vehículo.", "Error", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        carsView.showLoading(false);
                    }
                }
            };

            carsView.showLoading(true);
            worker.execute();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(carsView.getContentPanel(), "Año inválido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdateCar() {
        int selectedRow = carsView.getCarsTable().getSelectedRow();
        if (selectedRow < 0) return;

        try {
            CarResponseDto selectedCar = carsView.getTableModel().getCars().get(selectedRow);
            String make = carsView.getCarMakeField().getText();
            String model = carsView.getCarModelField().getText();
            int year = Integer.parseInt(carsView.getYearTextField().getText());

            UpdateCarRequestDto dto = new UpdateCarRequestDto(selectedCar.getId(), make, model, year);
            Long userId = user != null ? user.getId() : null;

            SwingWorker<CarResponseDto, Void> worker = new SwingWorker<>() {
                @Override
                protected CarResponseDto doInBackground() throws Exception {
                    Future<CarResponseDto> f = carService.updateCarAsync(dto, userId);
                    return f.get();
                }

                @Override
                protected void done() {
                    try {
                        CarResponseDto updatedCar = get();
                        if (updatedCar != null) {
                            notifyObservers(EventType.UPDATED, updatedCar);
                            carsView.clearFields();
                            loadCarsAsync();
                        } else {
                            JOptionPane.showMessageDialog(carsView.getContentPanel(), "No se pudo actualizar el vehículo.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(carsView.getContentPanel(), "Error al actualizar vehículo.", "Error", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        carsView.showLoading(false);
                    }
                }
            };

            carsView.showLoading(true);
            worker.execute();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(carsView.getContentPanel(), "Año inválido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteCar() {
        int selectedRow = carsView.getCarsTable().getSelectedRow();
        if (selectedRow < 0) return;

        CarResponseDto selectedCar = carsView.getTableModel().getCars().get(selectedRow);
        DeleteCarRequestDto dto = new DeleteCarRequestDto(selectedCar.getId());
        Long userId = user != null ? user.getId() : null;

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                Future<Boolean> f = carService.deleteCarAsync(dto, userId);
                return f.get();
            }

            @Override
            protected void done() {
                try {
                    Boolean success = get();
                    if (success != null && success) {
                        notifyObservers(EventType.DELETED, selectedCar.getId());
                        carsView.clearFields();
                        loadCarsAsync();
                    } else {
                        JOptionPane.showMessageDialog(carsView.getContentPanel(), "No se pudo borrar el vehículo.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(carsView.getContentPanel(), "Error al borrar vehículo.", "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    carsView.showLoading(false);
                }
            }
        };

        carsView.showLoading(true);
        worker.execute();
    }

    private void handleClearFields() {
        carsView.clearFields();
    }

    private void handleRowSelection(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            int row = carsView.getCarsTable().getSelectedRow();
            if (row >= 0) {
                CarResponseDto car = carsView.getTableModel().getCars().get(row);
                carsView.populateFields(car);
            }
        }
    }

    private void handleAddMaintenance() {
        int selectedRow = carsView.getCarsTable().getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(carsView.getContentPanel(), "Seleccione un vehículo primero.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JComboBox<MaintenanceType> typeCombo = new JComboBox<>(new MaintenanceType[]{
                MaintenanceType.REPAIR, MaintenanceType.MOD, MaintenanceType.ROUTINE
        });
        JTextArea descriptionArea = new JTextArea(6, 30);
        JScrollPane descScroll = new JScrollPane(descriptionArea);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("Tipo de mantenimiento:"));
        panel.add(typeCombo);
        panel.add(Box.createVerticalStrut(8));
        panel.add(new JLabel("Descripción:"));
        panel.add(descScroll);

        int result = JOptionPane.showConfirmDialog(carsView.getContentPanel(), panel, "Agregar mantenimiento", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        MaintenanceType selectedType = (MaintenanceType) typeCombo.getSelectedItem();
        String description = descriptionArea.getText().trim();
        if (selectedType == null || description.isEmpty()) {
            JOptionPane.showMessageDialog(carsView.getContentPanel(), "Tipo y descripción son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        CarResponseDto selectedCar = carsView.getTableModel().getCars().get(selectedRow);
        AddMaintenanceRequestDto dto = new AddMaintenanceRequestDto(description, selectedType, selectedCar.getId());
        Long userId = user != null ? user.getId() : null;

        carsView.showLoading(true);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private MaintenanceResponseDto created = null;
            private CarResponseDto updatedCar = null;

            @Override
            protected Void doInBackground() throws Exception {
                try {
                    var future = maintenanceService.addMaintenanceAsync(dto, userId);
                    created = future.get(); // puede ser null si falla
                    if (created != null) {
                        Future<List<CarResponseDto>> lf = carService.listCarsAsync(userId);
                        List<CarResponseDto> all = lf.get();
                        if (all != null) {
                            for (CarResponseDto c : all) {
                                if (c.getId().equals(created.getCarId())) {
                                    updatedCar = c;
                                    break;
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    throw ex;
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    if (created != null) {
                        List<MaintenanceResponseDto> toShow = new ArrayList<>();
                        try {
                            CarResponseDto carFromList = null;
                            if (updatedCar != null) carFromList = updatedCar;
                            else {
                                for (CarResponseDto c : carsView.getTableModel().getCars()) {
                                    if (c.getId().equals(created.getCarId())) { carFromList = c; break; }
                                }
                            }

                            if (carFromList != null) {
                                try {
                                    var mListObj = carFromList.getClass().getMethod("getMaintenances").invoke(carFromList);
                                    if (mListObj instanceof List) {
                                        @SuppressWarnings("unchecked")
                                        List<MaintenanceResponseDto> existing = (List<MaintenanceResponseDto>) mListObj;
                                        if (existing != null && !existing.isEmpty()) toShow.addAll(existing);
                                    }
                                } catch (NoSuchMethodException ignored) {

                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        toShow.add(0, created);
                        showMaintenanceDialogForList(created.getCarId(), toShow);

                        if (updatedCar != null) {
                            notifyObservers(EventType.UPDATED, updatedCar);
                        } else {
                            loadCarsAsync();
                        }

                        JOptionPane.showMessageDialog(carsView.getContentPanel(), "Mantenimiento agregado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        carsView.clearFields();
                    } else {
                        JOptionPane.showMessageDialog(carsView.getContentPanel(), "No se pudo agregar el mantenimiento.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    String msg = ex.getMessage() != null ? ex.getMessage() : "Error al agregar mantenimiento";
                    JOptionPane.showMessageDialog(carsView.getContentPanel(), "Error al agregar mantenimiento: " + msg, "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    carsView.showLoading(false);
                }
            }
        };

        worker.execute();
    }

    private void handleViewMaintenance() {
        int selectedRow = carsView.getCarsTable().getSelectedRow();
        System.out.println("[DEBUG] ViewMaintenance clicked, selectedRow=" + selectedRow);
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(carsView.getContentPanel(), "Seleccione un vehículo primero.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        CarResponseDto car = carsView.getTableModel().getCars().get(selectedRow);
        List<MaintenanceResponseDto> existing = null;
        try {
            existing = (List<MaintenanceResponseDto>) car.getClass().getMethod("getMaintenances").invoke(car);
        } catch (NoSuchMethodException nsme) {
            // no existe el método, seguir al fallback
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (existing != null && !existing.isEmpty()) {
            System.out.println("[DEBUG] Using maintenances from CarResponseDto, size=" + existing.size());
            showMaintenanceDialog(car, existing);
            return;
        }

        carsView.showLoading(true);
        SwingWorker<List<MaintenanceResponseDto>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<MaintenanceResponseDto> doInBackground() throws Exception {
                System.out.println("[DEBUG] Loading maintenances for carId=" + car.getId());
                Long userId = user != null ? user.getId() : null;
                var future = maintenanceService.listByCarAsync(car.getId(), userId);
                List<MaintenanceResponseDto> list = future.get();
                System.out.println("[DEBUG] Received maintenances list: " + (list == null ? "null" : list.size()));
                return list;
            }

            @Override
            protected void done() {
                try {
                    List<MaintenanceResponseDto> list = get();
                    if (list == null) list = List.of();

                    if (list.isEmpty()) {
                        JOptionPane.showMessageDialog(carsView.getContentPanel(), "No hay mantenimientos para este vehículo.", "Información", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    showMaintenanceDialog(car, list);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(carsView.getContentPanel(), "Error al cargar mantenimientos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    carsView.showLoading(false);
                }
            }
        };

        worker.execute();
    }

    private void showMaintenanceDialog(CarResponseDto car, List<MaintenanceResponseDto> list) {
        Window owner = SwingUtilities.getWindowAncestor(carsView.getContentPanel());
        JDialog dialog = new JDialog(owner, "Mantenimientos - " + car.getMake() + " " + car.getModel(), Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setSize(700, 400);
        dialog.setLocationRelativeTo(owner);

        Presentation.Models.MaintenanceTableModel tableModel = new Presentation.Models.MaintenanceTableModel();
        tableModel.setItems(list);
        JTable table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        JScrollPane scroll = new JScrollPane(table);

        JButton close = new JButton("Cerrar");
        close.addActionListener(ev -> dialog.dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(close);

        dialog.getContentPane().setLayout(new BorderLayout(8, 8));
        dialog.getContentPane().add(scroll, BorderLayout.CENTER);
        dialog.getContentPane().add(bottom, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(() -> dialog.setVisible(true));
    }

    private void showMaintenanceDialogForList(Long carId, List<MaintenanceResponseDto> list) {
        CarResponseDto car = null;
        for (CarResponseDto c : carsView.getTableModel().getCars()) {
            if (c.getId().equals(carId)) { car = c; break; }
        }
        String title = car != null ? "Mantenimientos - " + car.getMake() + " " + car.getModel() : "Mantenimientos";

        Window owner = SwingUtilities.getWindowAncestor(carsView.getContentPanel());
        JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setSize(700, 400);
        dialog.setLocationRelativeTo(owner);

        Presentation.Models.MaintenanceTableModel tableModel = new Presentation.Models.MaintenanceTableModel();
        tableModel.setItems(list);
        JTable table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);

        JScrollPane scroll = new JScrollPane(table);
        JButton close = new JButton("Cerrar");
        close.addActionListener(e -> dialog.dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(close);

        dialog.getContentPane().setLayout(new BorderLayout(8, 8));
        dialog.getContentPane().add(scroll, BorderLayout.CENTER);
        dialog.getContentPane().add(bottom, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(() -> dialog.setVisible(true));
    }
}
