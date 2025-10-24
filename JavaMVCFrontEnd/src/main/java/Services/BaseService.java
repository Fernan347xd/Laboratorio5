package Services;

import Domain.Dtos.RequestDto;
import Domain.Dtos.ResponseDto;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class BaseService {
    protected final String host;
    protected final int port;
    protected final Gson gson = new Gson();

    public BaseService(String host, int port) {
        this.host = host;
        this.port = port;
    }


    public ResponseDto sendRequest(RequestDto request) {
        int maxAttempts = 2;
        long backoffMs = 150;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            Socket socket = null;
            try {
                socket = new Socket(host, port);
                socket.setSoTimeout((int) Duration.ofSeconds(10).toMillis());

                String jsonRequest = gson.toJson(request);
                System.out.println("[DEBUG sendRequest] Sending request: " + jsonRequest);

                OutputStream outStream = socket.getOutputStream();
                PrintWriter out = new PrintWriter(outStream, true, StandardCharsets.UTF_8);
                out.println(jsonRequest);
                out.flush();
                socket.shutdownOutput();

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }
                String responseJson = sb.toString();
                System.out.println("[DEBUG sendRequest] Raw response: " + responseJson);

                if (responseJson == null || responseJson.isEmpty()) {
                    System.out.println("[DEBUG sendRequest] Empty response from server");
                    return null;
                }

                ResponseDto response = gson.fromJson(responseJson, ResponseDto.class);
                System.out.println("[DEBUG sendRequest] Parsed ResponseDto: success=" + (response != null && response.isSuccess()));
                return response;

            } catch (java.net.ConnectException ce) {
                System.err.println("[ERROR sendRequest] ConnectException: " + ce.getMessage());
                if (attempt < maxAttempts) {
                    try { Thread.sleep(backoffMs); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
                    backoffMs *= 2;
                    continue;
                }
                return null;
            } catch (Exception ex) {
                System.err.println("[ERROR sendRequest] " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
                ex.printStackTrace();
                return null;
            } finally {
                if (socket != null) {
                    try { socket.close(); } catch (Exception ignore) {}
                }
            }
        }
        return null;
    }
    
    protected ResponseDto sendRequest(String controller, String req, String data, Long userId) {
        String token = userId != null ? String.valueOf(userId) : null;
        RequestDto r = new RequestDto(controller, req, data, token);
        return sendRequest(r);
    }
}
