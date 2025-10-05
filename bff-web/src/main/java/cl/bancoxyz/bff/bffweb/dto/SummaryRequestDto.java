package cl.bancoxyz.bff.bffweb.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SummaryRequestDto {
  @Pattern(regexp = "^$|^\\d{4}-\\d{2}-\\d{2}$", message = "from debe tener formato YYYY-MM-DD")
  private String from;

  @Pattern(regexp = "^$|^\\d{4}-\\d{2}-\\d{2}$", message = "to debe tener formato YYYY-MM-DD")
  private String to;
}
