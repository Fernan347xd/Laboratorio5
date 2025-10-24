package Services;

import Domain.Dtos.RequestDto;
import Domain.Dtos.ResponseDto;
import Domain.Dtos.maintenance.AddMaintenanceRequestDto;
import Domain.Dtos.maintenance.ListMaintenanceResponseDto;
import Domain.Dtos.maintenance.MaintenanceResponseDto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MaintenanceService extends BaseService {
    private final ExecutorService executor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory());
    private final Gson gsonLocal = new Gson();

    private final Long contextUserId;

    public MaintenanceService(String host, int port) {
        this(host, port, null);
    }

    public MaintenanceService(String host, int port, Long userId) {
        super(host, port);
        this.contextUserId = userId;
    }

    private String resolveToken(Long userId) {
        Long u = userId != null ? userId : this.contextUserId;
        return u != null ? String.valueOf(u) : "";
    }

    public Future<MaintenanceResponseDto> addMaintenanceAsync(AddMaintenanceRequestDto dto, Long userId) {
        return executor.submit((Callable<MaintenanceResponseDto>) () -> {
            String payload = gsonLocal.toJson(dto);
            RequestDto primary = new RequestDto("Maintenance", "add", payload, resolveToken(userId));
            System.out.println("[MaintenanceService] Sending request to controller: " + primary.getController());
            ResponseDto response = sendRequest(primary);

            if (response == null) {
                System.out.println("[MaintenanceService] response == null");
                return null;
            }

            System.out.println("[MaintenanceService] response.message=" + response.getMessage() + " success=" + response.isSuccess());

            if (response.isSuccess() && response.getData() != null && !response.getData().isEmpty()) {
                try {
                    return gsonLocal.fromJson(response.getData(), MaintenanceResponseDto.class);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return null;
                }
            }

            if (response.getMessage() != null && response.getMessage().toLowerCase().contains("unknown")) {
                RequestDto fallback = new RequestDto("Maintenances", "add", payload, resolveToken(userId));
                System.out.println("[MaintenanceService] Fallback to controller: " + fallback.getController());
                ResponseDto resp2 = sendRequest(fallback);
                if (resp2 == null || !resp2.isSuccess() || resp2.getData() == null || resp2.getData().isEmpty()) {
                    System.out.println("[MaintenanceService] Fallback failed: " + (resp2 == null ? "null response" : resp2.getMessage()));
                    return null;
                }
                try {
                    return gsonLocal.fromJson(resp2.getData(), MaintenanceResponseDto.class);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return null;
                }
            }

            return null;
        });
    }

    public Future<List<MaintenanceResponseDto>> listByCarAsync(Long carId, Long userId) {
        return executor.submit(() -> {
            try {
                var payloadMap = new java.util.HashMap<String, Object>();
                payloadMap.put("carId", carId);
                String payload = gsonLocal.toJson(payloadMap);

                String[][] attempts = new String[][]{
                        {"Maintenance", "listByCar"},
                        {"Maintenances", "listByCar"}
                };
                String[] tokensToTry = new String[]{ resolveToken(userId), "" };

                java.lang.reflect.Type listType = new TypeToken<List<MaintenanceResponseDto>>() {}.getType();

                for (String[] attempt : attempts) {
                    for (String token : tokensToTry) {
                        RequestDto request = new RequestDto(attempt[0], attempt[1], payload, token);
                        System.out.println("[MaintenanceService] Trying request: controller=" + attempt[0] + " action=" + attempt[1] + " token=" + (token == null || token.isEmpty() ? "<empty>" : token));
                        ResponseDto response = sendRequest(request);

                        if (response == null) {
                            System.out.println("[MaintenanceService] null response for " + attempt[0] + "/" + attempt[1] + " token=" + token);
                            continue;
                        }

                        String data = response.getData();
                        System.out.println("[MaintenanceService] raw data: " + data + " (success=" + response.isSuccess() + " message=" + response.getMessage() + ")");
                        if (data == null || data.isEmpty()) continue;

                        try {
                            ListMaintenanceResponseDto wrapper = gsonLocal.fromJson(data, ListMaintenanceResponseDto.class);
                            if (wrapper != null && wrapper.getMaintenances() != null && !wrapper.getMaintenances().isEmpty()) {
                                System.out.println("[MaintenanceService] parsed wrapper maintenances size=" + wrapper.getMaintenances().size());
                                return wrapper.getMaintenances();
                            }
                        } catch (Exception ignored) {}

                        try {
                            List<MaintenanceResponseDto> directList = gsonLocal.fromJson(data, listType);
                            if (directList != null && !directList.isEmpty()) {
                                System.out.println("[MaintenanceService] parsed direct list size=" + directList.size());
                                return directList;
                            }
                        } catch (Exception ignored) {}

                        try {
                            java.util.Map<?, ?> wrapperMap = gsonLocal.fromJson(data, java.util.Map.class);
                            if (wrapperMap != null) {
                                String[] candidateKeys = {"maintenances", "data", "items", "list", "result"};
                                for (String key : candidateKeys) {
                                    if (wrapperMap.containsKey(key)) {
                                        Object inner = wrapperMap.get(key);
                                        if (inner != null) {
                                            String innerJson = gsonLocal.toJson(inner);
                                            try {
                                                List<MaintenanceResponseDto> innerList = gsonLocal.fromJson(innerJson, listType);
                                                if (innerList != null && !innerList.isEmpty()) {
                                                    System.out.println("[MaintenanceService] parsed wrapper key=" + key + " size=" + innerList.size());
                                                    return innerList;
                                                }
                                            } catch (Exception ignored) {}
                                        }
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            System.out.println("[MaintenanceService] wrapperMap parsing failed: " + ex.getMessage());
                        }

                        try {
                            MaintenanceResponseDto single = gsonLocal.fromJson(data, MaintenanceResponseDto.class);
                            if (single != null && single.getId() != null) {
                                List<MaintenanceResponseDto> singleList = new ArrayList<>();
                                singleList.add(single);
                                System.out.println("[MaintenanceService] parsed single object into list size=1");
                                return singleList;
                            }
                        } catch (Exception ignored) {}
                    }
                }

                System.out.println("[MaintenanceService] No maintenances found after attempts for carId=" + carId);
                return new ArrayList<>();
            } catch (Exception ex) {
                ex.printStackTrace();
                return new ArrayList<>();
            }
        });
    }
}
