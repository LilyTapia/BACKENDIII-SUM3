package cl.bancoxyz.bff.bffmobile.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDTO {
    String accountId;
    String date;
    String description;
    String type;
    double amount;
}