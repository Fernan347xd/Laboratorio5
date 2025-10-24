package Services;

import Domain.Dtos.RequestDto;
import Domain.Dtos.ResponseDto;
import Domain.Dtos.maintenance.AddMaintenanceRequestDto;
import Domain.Dtos.maintenance.MaintenanceResponseDto;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MaintenanceService extends BaseService {
    private final ExecutorService executor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory());
    private final Gson gsonLocal = new Gson();

    public MaintenanceService(String host, int port) {
        super(host, port);
    }

    public Future<MaintenanceResponseDto> addMaintenanceAsync(AddMaintenanceRequestDto dto, Long userId) {
        return executor.submit((Callable<MaintenanceResponseDto>) () -> {
            String payload = gsonLocal.toJson(dto);

            RequestDto primary = new RequestDto("Maintenance", "add", payload, userId.toString());
            System.out.println("[MaintenanceService] Sending request to controller: " + primary.getController());
            ResponseDto response = sendRequest(primary);

            if (response == null) {
                System.out.println("[MaintenanceService] response == null");
                return null;
            }

            System.out.println("[MaintenanceService] response.message=" + response.getMessage() + " success=" + response.isSuccess());

            if (response.isSuccess()) {
                try {
                    return gsonLocal.fromJson(response.getData(), MaintenanceResponseDto.class);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return null;
                }
            }

            if (response.getMessage() != null && response.getMessage().toLowerCase().contains("unknown")) {
                RequestDto fallback = new RequestDto("Maintenances", "add", payload, userId.toString());
                System.out.println("[MaintenanceService] Fallback to controller: " + fallback.getController());
                ResponseDto resp2 = sendRequest(fallback);
                if (resp2 == null || !resp2.isSuccess()) {
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
                java.util.Map<String,Object> payloadMap = new java.util.HashMap<>();
                payloadMap.put("carId", carId);
                String payload = gsonLocal.toJson(payloadMap);

                String[][] attempts = new String[][] {
                        {"Maintenance", "listByCar"},
                        {"Maintenances", "listByCar"}
                };
                String[] tokensToTry = new String[] { String.valueOf(userId), "" };

                java.lang.reflect.Type listType = new TypeToken<List<MaintenanceResponseDto>>() {}.getType();

                for (String[] attempt : attempts) {
                    for (String token : tokensToTry) {
                        RequestDto request = new RequestDto(attempt[0], attempt[1], payload, token);
                        System.out.println("[MaintenanceService] Trying request: controller=" + attempt[0] + " action=" + attempt[1] + " token=" + (token.isEmpty() ? "<empty>" : token));
                        ResponseDto response = sendRequest(request);
                        if (response == null) {
                            System.out.println("[MaintenanceService] null response for " + attempt[0] + "/" + attempt[1] + " token=" + token);
                            continue;
                        }

                        String data = response.getData();
                        System.out.println("[MaintenanceService] raw data: " + data + " (success=" + response.isSuccess() + " message=" + response.getMessage() + ")");
                        if (data == null || data.isEmpty()) continue;

                        // 1) intentar lista directa
                        try {
                            List<MaintenanceResponseDto> list = gsonLocal.fromJson(data, listType);
                            if (list != null && !list.isEmpty()) {
                                System.out.println("[MaintenanceService] parsed direct list size=" + list.size());
                                return list;
                            }
                        } catch (com.google.gson.JsonSyntaxException ex) {
                            // seguir a wrapper parsing
                        }

                        // 2) intentar wrapper con claves comunes
                        try {
                            java.util.Map<?,?> wrapper = gsonLocal.fromJson(data, java.util.Map.class);
                            if (wrapper != null) {
                                String[] candidateKeys = {"maintenances", "data", "items", "list", "result"};
                                for (String key : candidateKeys) {
                                    if (wrapper.containsKey(key)) {
                                        Object inner = wrapper.get(key);
                                        if (inner != null) {
                                            String innerJson = gsonLocal.toJson(inner);
                                            try {
                                                List<MaintenanceResponseDto> list = gsonLocal.fromJson(innerJson, listType);
                                                if (list != null && !list.isEmpty()) {
                                                    System.out.println("[MaintenanceService] parsed wrapper key=" + key + " size=" + list.size());
                                                    return list;
                                                }
                                            } catch (Exception ignored) {}
                                        }
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            System.out.println("[MaintenanceService] wrapper parsing failed: " + ex.getMessage());
                        }

                        // 3) intentar single object -> lista de 1
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