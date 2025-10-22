package Domain.Dtos;

public class ResponseDto {
    private boolean success;   // true if operation succeeded
    private String message;    // description or error message
    private String data;       // JSON of the response DTO (UserResponseDto, AuthResponseData, etc.)

    public ResponseDto() {}

    public ResponseDto(boolean success, String message, String data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
