package cl.bancoxyz.bff.bffweb.dto;

import java.util.List;

public record AccountWebDto(String accountId, Double balance, List<TransactionDTO> transactions) {}

