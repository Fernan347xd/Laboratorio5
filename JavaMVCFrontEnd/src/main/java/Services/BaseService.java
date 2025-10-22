package Services;

import Domain.Dtos.RequestDto;
import Domain.Dtos.ResponseDto;
import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;

public class BaseService {
    protected final String host;
    protected final int port;
    protected final Gson gson = new Gson();

    public BaseService(String host, int port) {
        this.host = host;
        this.port = port;
    }

    protected ResponseDto sendRequest(RequestDto request) {
        try (Socket socket = new Socket(host, port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            String requestJson = gson.toJson(request);
            writer.write(requestJson);
            writer.newLine();
            writer.flush();

            var responseJson = reader.readLine();
            return gson.fromJson(responseJson, ResponseDto.class);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
