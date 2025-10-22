package Domain.Dtos.cars;

import java.util.List;

public class ListCarsResponseDto {
    private List<CarResponseDto> cars;

    public ListCarsResponseDto() {}

    public ListCarsResponseDto(List<CarResponseDto> cars) {
        this.cars = cars;
    }

    public List<CarResponseDto> getCars() { return cars; }
    public void setCars(List<CarResponseDto> cars) { this.cars = cars; }
}
