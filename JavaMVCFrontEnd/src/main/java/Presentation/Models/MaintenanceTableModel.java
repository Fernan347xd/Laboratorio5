package Presentation.Models;

import Domain.Dtos.maintenance.MaintenanceResponseDto;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class MaintenanceTableModel extends AbstractTableModel {
    private final List<MaintenanceResponseDto> items = new ArrayList<>();
    private final String[] columns = {"Id", "Type", "Description", "Car Id", "Make", "Model"};

    public void setItems(List<MaintenanceResponseDto> list) {
        items.clear();
        if (list != null) items.addAll(list);
        fireTableDataChanged();
    }

    public List<MaintenanceResponseDto> getItems() { return items; }

    @Override
    public int getRowCount() { return items.size(); }

    @Override
    public int getColumnCount() { return columns.length; }

    @Override
    public String getColumnName(int column) { return columns[column]; }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        MaintenanceResponseDto m = items.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> m.getId();
            case 1 -> (m.getType() != null ? m.getType().name() : null);
            case 2 -> m.getDescription();
            case 3 -> m.getCarId();
            case 4 -> m.getCarMake();
            case 5 -> m.getCarModel();
            default -> null;
        };
    }
}
