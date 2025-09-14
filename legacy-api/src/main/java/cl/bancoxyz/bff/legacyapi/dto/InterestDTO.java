package cl.bancoxyz.bff.legacyapi.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterestDTO {
    String accountId;
    int month;
    double interest;
}