package cl.bancoxyz.bff.bffweb.dto;

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