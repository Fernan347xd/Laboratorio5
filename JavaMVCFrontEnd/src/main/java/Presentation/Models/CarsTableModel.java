package Presentation.Models;

import Domain.Dtos.cars.CarResponseDto;
import Presentation.IObserver;
import Utilities.EventType;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class CarsTableModel extends AbstractTableModel implements IObserver {

    private final List<CarResponseDto> cars = new ArrayList<>();
    private final String[] columnNames = {"ID", "Make", "Model", "Year", "Owner ID"};

    // -------------------------
    // AbstractTableModel methods
    // -------------------------
    @Override
    public int getRowCount() {
        return cars.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        CarResponseDto car = cars.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> car.getId();
            case 1 -> car.getMake();
            case 2 -> car.getModel();
            case 3 -> car.getYear();
            case 4 -> car.getOwner().getUsername();
            default -> null;
        };
    }

    // -------------------------
    // Observer implementation
    // -------------------------
    @Override
    public void update(EventType eventType, Object data) {
        if (data == null) return;

        switch (eventType) {
            case CREATED -> {
                CarResponseDto newCar = (CarResponseDto) data;
                cars.add(newCar);
                fireTableRowsInserted(cars.size() - 1, cars.size() - 1);
            }
            case UPDATED -> {
                CarResponseDto updatedCar = (CarResponseDto) data;
                for (int i = 0; i < cars.size(); i++) {
                    if (cars.get(i).getId().equals(updatedCar.getId())) {
                        cars.set(i, updatedCar);
                        fireTableRowsUpdated(i, i);
                        break;
                    }
                }
            }
            case DELETED -> {
                Long deletedId = (Long) data;
                for (int i = 0; i < cars.size(); i++) {
                    if (cars.get(i).getId().equals(deletedId)) {
                        cars.remove(i);
                        fireTableRowsDeleted(i, i);
                        break;
                    }
                }
            }
        }
    }

    // -------------------------
    // Utility methods
    // -------------------------
    public List<CarResponseDto> getCars() {
        return new ArrayList<>(cars);
    }

    public void setCars(List<CarResponseDto> newCars) {
        cars.clear();
        if (newCars != null) cars.addAll(newCars);
        fireTableDataChanged();
    }
}