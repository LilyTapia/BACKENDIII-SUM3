package cl.bancoxyz.analytics.dto;

import lombok.Data;

@Data
public class AnnualAccountDto {
  private String accountId;
  private String year;
  private double openingBalance;
  private double closingBalance;
}
