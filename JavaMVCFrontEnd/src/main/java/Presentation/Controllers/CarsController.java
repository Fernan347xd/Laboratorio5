package Presentation.Controllers;

import Domain.Dtos.cars.AddCarRequestDto;
import Domain.Dtos.cars.CarResponseDto;
import Domain.Dtos.cars.DeleteCarRequestDto;
import Domain.Dtos.cars.UpdateCarRequestDto;
import Presentation.Observable;
import Presentation.Views.CarsView;
import Services.CarService;
import Utilities.EventType;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.util.List;

public class CarsController extends Observable {
    private final CarsView carsView;
    private final CarService carService;

    public CarsController(CarsView carsView, CarService carService) {
        this.carsView = carsView;
        this.carService = carService;

        addObserver(carsView.getTableModel());
        loadCarsAsync();
        addListeners();
    }

    private void loadCarsAsync() {
        carsView.showLoading(true);

        SwingWorker<List<CarResponseDto>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<CarResponseDto> doInBackground() throws Exception {
                return carService.listCarsAsync(1L).get();
            }

            @Override
            protected void done() {
                try {
                    List<CarResponseDto> cars = get();
                    carsView.getTableModel().setCars(cars);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    carsView.showLoading(false);
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
    }

    // ---------------------------
    // Action Handlers
    // ---------------------------
    private void handleAddCar() {
        String make = carsView.getCarMakeField().getText();
        String model = carsView.getCarModelField().getText();
        int year = Integer.parseInt(carsView.getYearTextField().getText());

        AddCarRequestDto dto = new AddCarRequestDto(make, model, year, 1L);

        SwingWorker<CarResponseDto, Void> worker = new SwingWorker<>() {
            @Override
            protected CarResponseDto doInBackground() throws Exception {
                return carService.addCarAsync(dto, 1L).get();
            }

            @Override
            protected void done() {
                try {
                    CarResponseDto car = get();
                    notifyObservers(EventType.CREATED, car);
                    carsView.clearFields();
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    carsView.showLoading(false);
                }
            }
        };

        carsView.showLoading(true);
        worker.execute();
    }

    private void handleUpdateCar() {
        int selectedRow = carsView.getCarsTable().getSelectedRow();
        if (selectedRow < 0) return;

        CarResponseDto selectedCar = carsView.getTableModel().getCars().get(selectedRow);
        String make = carsView.getCarMakeField().getText();
        String model = carsView.getCarModelField().getText();
        int year = Integer.parseInt(carsView.getYearTextField().getText());

        UpdateCarRequestDto dto = new UpdateCarRequestDto(selectedCar.getId(), make, model, year);

        SwingWorker<CarResponseDto, Void> worker = new SwingWorker<>() {
            @Override
            protected CarResponseDto doInBackground() throws Exception {
                return carService.updateCarAsync(dto, 1L).get();
            }

            @Override
            protected void done() {
                try {
                    CarResponseDto updatedCar = get();
                    notifyObservers(EventType.UPDATED, updatedCar);
                    carsView.clearFields();
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    carsView.showLoading(false);
                }
            }
        };

        carsView.showLoading(true);
        worker.execute();
    }

    private void handleDeleteCar() {
        int selectedRow = carsView.getCarsTable().getSelectedRow();
        if (selectedRow < 0) return;

        CarResponseDto selectedCar = carsView.getTableModel().getCars().get(selectedRow);
        DeleteCarRequestDto dto = new DeleteCarRequestDto(selectedCar.getId());

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return carService.deleteCarAsync(dto, 1L).get();
            }

            @Override
            protected void done() {
                try {
                    Boolean success = get();
                    if (success) notifyObservers(EventType.DELETED, selectedCar.getId());
                    carsView.clearFields();
                } catch (Exception ex) {
                    ex.printStackTrace();
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
}
