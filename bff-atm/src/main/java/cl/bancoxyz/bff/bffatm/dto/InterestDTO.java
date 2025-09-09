package cl.bancoxyz.bff.bffatm.dto;

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