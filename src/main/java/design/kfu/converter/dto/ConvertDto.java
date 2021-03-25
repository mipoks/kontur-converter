package design.kfu.converter.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConvertDto {
    private String from;
    private String to;

    public ConvertDto(String from, String to) {
        this.from = from;
        this.to = to;
    }
}
