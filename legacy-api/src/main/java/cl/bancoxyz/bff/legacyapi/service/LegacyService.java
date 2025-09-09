package cl.bancoxyz.bff.legacyapi.service;

import cl.bancoxyz.bff.legacyapi.dto.*;
import cl.bancoxyz.bff.legacyapi.infra.LegacyCsvClient;
import org.springframework.stereotype.Service;

import com.opencsv.exceptions.CsvValidationException;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LegacyService {
  private final LegacyCsvClient client;

  public LegacyService(LegacyCsvClient c) {
    this.client = c;
  }

  public List<TransactionDTO> transactions(String accountId, String from, String to) throws CsvValidationException {
    LocalDate f = from != null ? LocalDate.parse(from) : LocalDate.MIN;
    LocalDate t = to != null ? LocalDate.parse(to) : LocalDate.MAX;
    return client.transacciones().stream()
        .filter(
            r -> accountId == null || accountId.equals(r.getOrDefault("account_id", r.getOrDefault("accountId", ""))))
        .map(r -> TransactionDTO.builder()
            .accountId(String.valueOf(r.getOrDefault("account_id", r.getOrDefault("accountId", ""))))
            .date(r.getOrDefault("date", "")).description(r.getOrDefault("description", ""))
            .type(r.getOrDefault("type", "")).amount(Double.parseDouble(r.getOrDefault("amount", "0"))).build())
        .filter(tx -> {
          try {
            var d = java.time.LocalDate.parse(tx.getDate().replace("/", "-"));
            return !d.isBefore(f) && !d.isAfter(t);
          } catch (Exception e) {
            return false;
          }
        })
        .collect(Collectors.toList());
  }

  public List<InterestDTO> interests(String accountId, Integer month) throws CsvValidationException {
    return client.intereses().stream()
        .filter(
            r -> accountId == null || accountId.equals(r.getOrDefault("account_id", r.getOrDefault("accountId", ""))))
        .filter(r -> month == null || Integer.parseInt(r.getOrDefault("month", "0")) == month)
        .map(r -> InterestDTO.builder()
            .accountId(String.valueOf(r.getOrDefault("account_id", r.getOrDefault("accountId", ""))))
            .month(Integer.parseInt(r.getOrDefault("month", "0")))
            .interest(Double.parseDouble(r.getOrDefault("interest", "0"))).build())
        .toList();
  }

  public List<AnnualAccountDTO> annual(String accountId, String year) throws CsvValidationException {
    return client.cuentasAnuales().stream()
        .filter(
            r -> accountId == null || accountId.equals(r.getOrDefault("account_id", r.getOrDefault("accountId", ""))))
        .filter(r -> year == null || year.equals(r.getOrDefault("year", "")))
        .map(r -> AnnualAccountDTO.builder()
            .accountId(String.valueOf(r.getOrDefault("account_id", r.getOrDefault("accountId", ""))))
            .year(r.getOrDefault("year", "")).openingBalance(Double.parseDouble(r.getOrDefault("opening_balance", "0")))
            .closingBalance(Double.parseDouble(r.getOrDefault("closing_balance", "0"))).build())
        .toList();
  }
}