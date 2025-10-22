package Domain.Dtos.cars;

public class DeleteCarRequestDto {
    private Long id;

    public DeleteCarRequestDto() {}
    public DeleteCarRequestDto(Long id) { this.id = id; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}