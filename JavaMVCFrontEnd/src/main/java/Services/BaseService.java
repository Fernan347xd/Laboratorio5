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
        Socket socket = null;
        try {
            socket = new Socket(host, port);
            // Opcional: setSoTimeout para no bloquear indefinidamente al leer
            socket.setSoTimeout((int) Duration.ofSeconds(10).toMillis());

            String jsonRequest = gson.toJson(request);
            System.out.println("[DEBUG sendRequest] Sending request: " + jsonRequest);

            // Escribir petición y forzar flush + shutdownOutput para indicar EOF al servidor
            OutputStream outStream = socket.getOutputStream();
            PrintWriter out = new PrintWriter(outStream, true, StandardCharsets.UTF_8);
            out.println(jsonRequest);
            out.flush();
            socket.shutdownOutput();

            // Leer respuesta hasta EOF
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
}
