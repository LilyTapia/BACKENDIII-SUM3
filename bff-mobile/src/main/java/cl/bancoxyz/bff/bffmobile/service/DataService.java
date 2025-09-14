package cl.bancoxyz.bff.bffmobile.service;

import cl.bancoxyz.bff.bffmobile.dto.*;
import cl.bancoxyz.bff.bffmobile.client.LegacyApiClient;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class DataService {
  private final LegacyApiClient legacy;

  public DataService(LegacyApiClient l) {
    this.legacy = l;
  }

  public List<TransactionDTO> transactions(String accountId, String from, String to) {
    return legacy.transactions(accountId, from, to);
  }

  public List<InterestDTO> interests(String accountId, Integer month) {
    return legacy.interests(accountId, month);
  }

  public List<AnnualAccountDTO> annual(String accountId, String year) {
    return legacy.annual(accountId, year);
  }
}