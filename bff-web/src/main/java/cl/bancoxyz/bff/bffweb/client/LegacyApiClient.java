package cl.bancoxyz.bff.bffweb.client;

import cl.bancoxyz.bff.bffweb.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@FeignClient(name = "legacy-api", path = "/legacy")
public interface LegacyApiClient {
  @GetMapping("/transactions")
  List<TransactionDTO> transactions(
      @RequestParam(value = "accountId", required = false) String accountId,
      @RequestParam(value = "from", required = false) String from,
      @RequestParam(value = "to", required = false) String to);

  @GetMapping("/interests")
  List<InterestDTO> interests(
      @RequestParam(value = "accountId", required = false) String accountId,
      @RequestParam(value = "month", required = false) Integer month);

  @GetMapping("/annual")
  List<AnnualAccountDTO> annual(
      @RequestParam(value = "accountId", required = false) String accountId,
      @RequestParam(value = "year", required = false) String year);
}
