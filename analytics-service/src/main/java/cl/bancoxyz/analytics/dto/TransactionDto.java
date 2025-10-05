package cl.bancoxyz.analytics.dto;

import lombok.Data;

@Data
public class TransactionDto {
  private String accountId;
  private String transactionDate;
  private double amount;
  private String description;
}
