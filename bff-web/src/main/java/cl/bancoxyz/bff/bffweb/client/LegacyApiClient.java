package cl.bancoxyz.bff.bffweb.client;

import cl.bancoxyz.bff.bffweb.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@FeignClient(name = "legacy", url = "${legacy.api.url}")
public interface LegacyApiClient {
  @GetMapping("/legacy/transactions")
  List<TransactionDTO> transactions(
      @RequestParam(value = "accountId", required = false) String accountId,
      @RequestParam(value = "from", required = false) String from,
      @RequestParam(value = "to", required = false) String to);

  @GetMapping("/legacy/interests")
  List<InterestDTO> interests(
      @RequestParam(value = "accountId", required = false) String accountId,
      @RequestParam(value = "month", required = false) Integer month);

  @GetMapping("/legacy/annual")
  List<AnnualAccountDTO> annual(
      @RequestParam(value = "accountId", required = false) String accountId,
      @RequestParam(value = "year", required = false) String year);
}
