package Services;

import Domain.Dtos.RequestDto;
import Domain.Dtos.ResponseDto;
import Domain.Dtos.auth.LoginRequestDto;
import Domain.Dtos.auth.UserResponseDto;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AuthService extends BaseService {
    // Ejecutor para Hilos.
    private final ExecutorService executor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory());

    public AuthService(String host, int port) {
        super(host, port);
    }

    public Future<UserResponseDto> login(String usernameOrEmail, String password) {
        return executor.submit(() -> {
            LoginRequestDto loginDto = new LoginRequestDto(usernameOrEmail, password);
            RequestDto request = new RequestDto(
                    "Auth",
                    "login",
                    gson.toJson(loginDto),
                    null
            );

            ResponseDto response = sendRequest(request);
            if (response == null || !response.isSuccess() || response.getData() == null) {
                System.out.println("[AuthService-client] login failed or null response");
                return null;
            }

            try {
                return gson.fromJson(response.getData(), UserResponseDto.class);
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        });
    }

    public Future<UserResponseDto> register(String username, String email, String password) {
        return executor.submit(() -> {
            Map<String, String> payload = new HashMap<>();
            payload.put("username", username);
            payload.put("email", email);
            payload.put("password", password);
            payload.put("role", "USER"); // <-- agregamos role por defecto

            String payloadJson = gson.toJson(payload);
            System.out.println("[AuthService-client] register payload: " + payloadJson);

            RequestDto request = new RequestDto(
                    "Auth",
                    "register",
                    payloadJson,
                    null
            );

            ResponseDto response = sendRequest(request);
            System.out.println("[AuthService-client] register response: " + (response == null ? "null" : ("success=" + response.isSuccess() + " message=" + response.getMessage() + " data=" + response.getData())));
            if (response == null || !response.isSuccess() || response.getData() == null) {
                return null;
            }

            try {
                return gson.fromJson(response.getData(), UserResponseDto.class);
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        });
    }
}
