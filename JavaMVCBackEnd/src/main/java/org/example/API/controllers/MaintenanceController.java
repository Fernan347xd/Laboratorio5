package org.example.API.controllers;

import basecontroller.IBaseController;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.example.Domain.dtos.RequestDto;
import org.example.Domain.dtos.ResponseDto;
import org.example.Domain.dtos.maintenance.AddMaintenanceRequestDto;
import org.example.Domain.dtos.maintenance.UpdateMaintenanceRequestDto;
import org.example.Domain.dtos.maintenance.DeleteMaintenanceRequestDto;
import org.example.Domain.dtos.maintenance.MaintenanceResponseDto;
import org.example.Domain.dtos.maintenance.ListMaintenanceResponseDto;

import org.example.Domain.models.Car;
import org.example.Domain.models.Maintenance;

import org.example.DataAccess.services.MaintenanceService;
import org.example.DataAccess.services.CarService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MaintenanceController implements IBaseController<ResponseDto, RequestDto> {

    private final MaintenanceService maintenanceService;
    private final CarService carService;
    private final Gson gson = new Gson();

    public MaintenanceController(MaintenanceService maintenanceService, CarService carService) {
        this.maintenanceService = maintenanceService;
        this.carService = carService;
    }

    @Override
    public String getControllerName() {
        return "Maintenance";
    }

    @Override
    @SuppressWarnings("unused")
    public ResponseDto route(RequestDto request) {
        try {
            switch (request.getRequest()) {
                case "add":
                    return handleAddMaintenance(request);
                case "update":
                    return handleUpdateMaintenance(request);
                case "delete":
                    return handleDeleteMaintenance(request);
                case "listByCar":
                    return handleListByCar(request);
                case "get":
                    return handleGetMaintenance(request);
                default:
                    return new ResponseDto(false, "Unknown request: " + request.getRequest(), null);
            }
        } catch (Exception e) {
            System.out.println("Error in route: " + e.getMessage());
            e.printStackTrace();
            return new ResponseDto(false, e.getMessage(), null);
        }
    }

    private ResponseDto handleAddMaintenance(RequestDto request) {
        try {
            if (request.getToken() == null || request.getToken().isEmpty()) {
                return new ResponseDto(false, "Unauthorized", null);
            }

            AddMaintenanceRequestDto dto = gson.fromJson(request.getData(), AddMaintenanceRequestDto.class);

            Car car = carService.getCarById(dto.getCarId());
            if (car == null) {
                return new ResponseDto(false, "Car not found", null);
            }

            Maintenance maintenance = maintenanceService.createMaintenance(dto.getDescription(), dto.getType(), car);

            MaintenanceResponseDto response = toResponseDto(maintenance);
            return new ResponseDto(true, "Maintenance added successfully", gson.toJson(response));
        } catch (Exception e) {
            System.out.println("Error in handleAddMaintenance: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private ResponseDto handleUpdateMaintenance(RequestDto request) {
        try {
            if (request.getToken() == null || request.getToken().isEmpty()) {
                return new ResponseDto(false, "Unauthorized", null);
            }

            UpdateMaintenanceRequestDto dto = gson.fromJson(request.getData(), UpdateMaintenanceRequestDto.class);

            Car car = null;
            if (dto.getCarId() != null) {
                car = carService.getCarById(dto.getCarId());
                if (car == null) {
                    return new ResponseDto(false, "Car not found", null);
                }
            }

            Maintenance updated = maintenanceService.updateMaintenance(Math.toIntExact(dto.getId()), dto.getDescription(), dto.getType(), car);

            if (updated == null) {
                return new ResponseDto(false, "Maintenance not found", null);
            }

            MaintenanceResponseDto response = toResponseDto(updated);
            return new ResponseDto(true, "Maintenance updated successfully", gson.toJson(response));
        } catch (Exception e) {
            System.out.println("Error in handleUpdateMaintenance: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private ResponseDto handleDeleteMaintenance(RequestDto request) {
        try {
            if (request.getToken() == null || request.getToken().isEmpty()) {
                return new ResponseDto(false, "Unauthorized", null);
            }

            DeleteMaintenanceRequestDto dto = gson.fromJson(request.getData(), DeleteMaintenanceRequestDto.class);
            boolean deleted = maintenanceService.deleteMaintenance(dto.getId());

            if (!deleted) {
                return new ResponseDto(false, "Maintenance not found or could not be deleted", null);
            }

            return new ResponseDto(true, "Maintenance deleted successfully", null);
        } catch (Exception e) {
            System.out.println("Error in handleDeleteMaintenance: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private ResponseDto handleListByCar(RequestDto request) {
        try {
            if (request.getToken() == null || request.getToken().isEmpty()) {
                return new ResponseDto(false, "Unauthorized", null);
            }

            System.out.println("[MaintenanceController] handleListByCar raw payload: " + request.getData() + " token=" + request.getToken());

            Long carId;
            try {
                JsonObject json = gson.fromJson(request.getData(), JsonObject.class);
                if (json == null || !json.has("carId") || json.get("carId").isJsonNull()) {
                    System.out.println("[MaintenanceController] handleListByCar missing carId in payload");
                    ListMaintenanceResponseDto emptyWrapper = new ListMaintenanceResponseDto(Collections.emptyList());
                    return new ResponseDto(false, "Missing carId", gson.toJson(emptyWrapper));
                }
                carId = json.get("carId").getAsLong();
            } catch (Exception parseEx) {
                System.out.println("[MaintenanceController] handleListByCar parse error: " + parseEx.getMessage());
                ListMaintenanceResponseDto emptyWrapper = new ListMaintenanceResponseDto(Collections.emptyList());
                return new ResponseDto(false, "Invalid payload for carId", gson.toJson(emptyWrapper));
            }

            System.out.println("[MaintenanceController] handleListByCar parsed carId=" + carId);

            Car car = carService.getCarById(carId);
            if (car == null) {
                System.out.println("[MaintenanceController] handleListByCar car not found for id=" + carId);
                ListMaintenanceResponseDto emptyWrapper = new ListMaintenanceResponseDto(Collections.emptyList());
                return new ResponseDto(false, "Car not found", gson.toJson(emptyWrapper));
            }

            List<Maintenance> maintenanceList = maintenanceService.getAllMaintenanceByCarId(carId);
            System.out.println("[MaintenanceController] handleListByCar service returned size=" + (maintenanceList == null ? 0 : maintenanceList.size()));

            if (maintenanceList == null || maintenanceList.isEmpty()) {
                ListMaintenanceResponseDto emptyWrapper = new ListMaintenanceResponseDto(Collections.emptyList());
                return new ResponseDto(true, "Maintenance list retrieved successfully", gson.toJson(emptyWrapper));
            }

            List<MaintenanceResponseDto> dtos = maintenanceList.stream()
                    .map(this::toResponseDto)
                    .collect(Collectors.toList());

            ListMaintenanceResponseDto response = new ListMaintenanceResponseDto(dtos);
            String data = gson.toJson(response);
            System.out.println("[MaintenanceController] handleListByCar returning data: " + data);
            return new ResponseDto(true, "Maintenance list retrieved successfully", data);
        } catch (Exception e) {
            System.out.println("Error in handleListByCar: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private ResponseDto handleGetMaintenance(RequestDto request) {
        try {
            if (request.getToken() == null || request.getToken().isEmpty()) {
                return new ResponseDto(false, "Unauthorized", null);
            }

            DeleteMaintenanceRequestDto dto = gson.fromJson(request.getData(), DeleteMaintenanceRequestDto.class);
            Maintenance maintenance = maintenanceService.getMaintenanceById(dto.getId());

            if (maintenance == null) {
                return new ResponseDto(false, "Maintenance not found", null);
            }

            MaintenanceResponseDto response = toResponseDto(maintenance);
            return new ResponseDto(true, "Maintenance retrieved successfully", gson.toJson(response));
        } catch (Exception e) {
            System.out.println("Error in handleGetMaintenance: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private MaintenanceResponseDto toResponseDto(Maintenance m) {
        Long carId = m.getCarMaintenance() != null ? m.getCarMaintenance().getId() : null;
        String carMake = m.getCarMaintenance() != null ? m.getCarMaintenance().getMake() : null;
        String carModel = m.getCarMaintenance() != null ? m.getCarMaintenance().getModel() : null;

        return new MaintenanceResponseDto(
                (long) m.getId(),
                m.getDescription(),
                m.getType(),
                carId,
                carMake,
                carModel
        );
    }
}
