package cl.bancoxyz.bff.legacyapi.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnualAccountDTO {
    String accountId;
    String year;
    double openingBalance;
    double closingBalance;
}