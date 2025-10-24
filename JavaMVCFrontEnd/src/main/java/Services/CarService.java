package Services;

import Domain.Dtos.RequestDto;
import Domain.Dtos.ResponseDto;
import Domain.Dtos.cars.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CarService extends BaseService {

    private final ExecutorService executor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory());
    private final Long contextUserId;

    public CarService(String host, int port) {
        this(host, port, null);
    }

    public CarService(String host, int port, Long userId) {
        super(host, port);
        this.contextUserId = userId;
    }

    private String resolveUserId(Long userIdParam) {
        Long u = userIdParam != null ? userIdParam : contextUserId;
        return u != null ? u.toString() : "";
    }

    public Future<CarResponseDto> addCarAsync(AddCarRequestDto dto, Long userId) {
        return executor.submit(() -> {
            RequestDto request = new RequestDto("Cars", "add", gson.toJson(dto), resolveUserId(userId));
            ResponseDto response = sendRequest(request);
            if (response == null || !response.isSuccess()) return null;
            return gson.fromJson(response.getData(), CarResponseDto.class);
        });
    }

    public Future<CarResponseDto> updateCarAsync(UpdateCarRequestDto dto, Long userId) {
        return executor.submit(() -> {
            RequestDto request = new RequestDto("Cars", "update", gson.toJson(dto), resolveUserId(userId));
            ResponseDto response = sendRequest(request);
            if (response == null || !response.isSuccess()) return null;
            return gson.fromJson(response.getData(), CarResponseDto.class);
        });
    }

    public Future<Boolean> deleteCarAsync(DeleteCarRequestDto dto, Long userId) {
        return executor.submit(() -> {
            RequestDto request = new RequestDto("Cars", "delete", gson.toJson(dto), resolveUserId(userId));
            ResponseDto response = sendRequest(request);
            return response != null && response.isSuccess();
        });
    }

    public Future<List<CarResponseDto>> listCarsAsync(Long userId) {
        return executor.submit(() -> {
            RequestDto request = new RequestDto("Cars", "list", "", resolveUserId(userId));
            ResponseDto response = sendRequest(request);
            if (response == null || !response.isSuccess()) return null;
            ListCarsResponseDto listResponse = gson.fromJson(response.getData(), ListCarsResponseDto.class);
            return listResponse.getCars();
        });
    }
}
