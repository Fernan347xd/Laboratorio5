package Domain.Dtos.cars;

public class AddCarRequestDto {
    private String make;
    private String model;
    private int year;
    private Long ownerId; // We only send the user ID, not the whole User object

    public AddCarRequestDto() {}

    public AddCarRequestDto(String make, String model, int year, Long ownerId) {
        this.make = make;
        this.model = model;
        this.year = year;
        this.ownerId = ownerId;
    }

    // Getters & Setters
    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
}
