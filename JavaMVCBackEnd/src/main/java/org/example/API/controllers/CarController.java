package org.example.API.controllers;

import com.google.gson.Gson;
import org.example.Domain.dtos.RequestDto;
import org.example.Domain.dtos.ResponseDto;
import org.example.Domain.dtos.auth.UserResponseDto;
import org.example.Domain.dtos.cars.*;
import org.example.Domain.models.Car;
import org.example.DataAccess.services.CarService;

import java.util.List;
import java.util.stream.Collectors;

public class CarController {

    private final CarService carService;
    private final Gson gson = new Gson();

    public CarController(CarService carService) {
        this.carService = carService;
    }

    public ResponseDto route(RequestDto request) {
        try {
            switch (request.getRequest()) {
                case "add":
                    return handleAddCar(request);
                case "update":
                    return handleUpdateCar(request);
                case "delete":
                    return handleDeleteCar(request);
                case "list":
                    return handleListCars(request);
                case "get":
                    return handleGetCar(request);
                default:
                    return new ResponseDto(false, "Unknown request: " + request.getRequest(), null);
            }
        } catch (Exception e) {
            return new ResponseDto(false, e.getMessage(), null);
        }
    }

    // --- ADD CAR ---
    private ResponseDto handleAddCar(RequestDto request) {
        try {
            Long userId = parseTokenToUserId(request);
            if (userId == null) return new ResponseDto(false, "Unauthorized", null);

            AddCarRequestDto dto = gson.fromJson(request.getData(), AddCarRequestDto.class);

            // Ensure ownerId in DTO is either null or equals the authenticated user
            Long ownerIdFromDto = dto.getOwnerId();
            if (ownerIdFromDto != null && !ownerIdFromDto.equals(userId)) {
                return new ResponseDto(false, "Owner mismatch", null);
            }

            Car car = carService.createCar(dto.getMake(), dto.getModel(), dto.getYear(), userId);

            CarResponseDto response = toResponseDto(car);
            return new ResponseDto(true, "Car added successfully", gson.toJson(response));
        } catch (Exception e) {
            System.out.println("Error in handleAddCar: " + e.getMessage());
            throw e;
        }
    }

    // --- UPDATE CAR ---
    private ResponseDto handleUpdateCar(RequestDto request) {
        try {
            Long userId = parseTokenToUserId(request);
            if (userId == null) return new ResponseDto(false, "Unauthorized", null);

            UpdateCarRequestDto dto = gson.fromJson(request.getData(), UpdateCarRequestDto.class);

            // Verify ownership before updating
            Car existing = carService.getCarById(dto.getId());
            if (existing == null) return new ResponseDto(false, "Car not found", null);
            if (existing.getOwner() == null || !userId.equals(existing.getOwner().getId())) {
                return new ResponseDto(false, "Forbidden: not the owner", null);
            }

            Car updated = carService.updateCar(dto.getId(), dto.getMake(), dto.getModel(), dto.getYear());

            if (updated == null) {
                return new ResponseDto(false, "Car not found", null);
            }

            CarResponseDto response = toResponseDto(updated);
            return new ResponseDto(true, "Car updated successfully", gson.toJson(response));
        } catch (Exception e) {
            System.out.println("Error in handleUpdateCar: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // --- DELETE CAR ---
    private ResponseDto handleDeleteCar(RequestDto request) {
        try {
            Long userId = parseTokenToUserId(request);
            if (userId == null) return new ResponseDto(false, "Unauthorized", null);

            DeleteCarRequestDto dto = gson.fromJson(request.getData(), DeleteCarRequestDto.class);
            Car existing = carService.getCarById(dto.getId());
            if (existing == null) return new ResponseDto(false, "Car not found", null);
            if (existing.getOwner() == null || !userId.equals(existing.getOwner().getId())) {
                return new ResponseDto(false, "Forbidden: not the owner", null);
            }

            boolean deleted = carService.deleteCar(dto.getId());

            if (!deleted) {
                return new ResponseDto(false, "Car not found or could not be deleted", null);
            }

            return new ResponseDto(true, "Car deleted successfully", null);
        } catch (Exception e) {
            System.out.println("Error in handleDeleteCar: " + e.getMessage());
            throw e;
        }
    }

    // --- LIST CARS ---
    private ResponseDto handleListCars(RequestDto request) {
        try {
            Long userId = parseTokenToUserId(request);
            if (userId == null) return new ResponseDto(false, "Unauthorized", null);

            // Use service method that filters by owner id
            List<Car> cars = carService.getCarsByUserId(userId);
            List<CarResponseDto> carDtos = cars.stream()
                    .map(this::toResponseDto)
                    .collect(Collectors.toList());

            ListCarsResponseDto response = new ListCarsResponseDto(carDtos);
            return new ResponseDto(true, "Cars retrieved successfully", gson.toJson(response));
        } catch (Exception e) {
            System.out.println("Error in handleListCars: " + e.getMessage());
            throw e;
        }
    }

    // --- GET SINGLE CAR ---
    private ResponseDto handleGetCar(RequestDto request) {
        try {
            Long userId = parseTokenToUserId(request);
            if (userId == null) return new ResponseDto(false, "Unauthorized", null);

            DeleteCarRequestDto dto = gson.fromJson(request.getData(), DeleteCarRequestDto.class);
            Car car = carService.getCarById(dto.getId());

            if (car == null) {
                return new ResponseDto(false, "Car not found", null);
            }

            if (car.getOwner() == null || !userId.equals(car.getOwner().getId())) {
                return new ResponseDto(false, "Forbidden: not the owner", null);
            }

            CarResponseDto response = toResponseDto(car);
            return new ResponseDto(true, "Car retrieved successfully", gson.toJson(response));
        } catch (Exception e) {
            System.out.println("Error in handleGetCar: " + e.getMessage());
            throw e;
        }
    }

    // --- Helper method ---
    private CarResponseDto toResponseDto(Car car) {
        var owner =  new UserResponseDto(
                car.getOwner().getId(),
                car.getOwner().getUsername(),
                car.getOwner().getEmail(),
                car.getOwner().getRole(),
                car.getOwner().getCreatedAt().toString(),
                car.getOwner().getUpdatedAt().toString()
        );

        return new CarResponseDto(
                car.getId(),
                car.getMake(),
                car.getModel(),
                car.getYear(),
                owner,
                car.getCreatedAt().toString(),
                car.getUpdatedAt().toString()
        );
    }

    // Parse the token field (expected to be userId) into Long, or return null if invalid
    private Long parseTokenToUserId(RequestDto request) {
        try {
            String token = request.getToken();
            if (token == null || token.isBlank()) return null;
            return Long.valueOf(token);
        } catch (Exception e) {
            return null;
        }
    }
}
