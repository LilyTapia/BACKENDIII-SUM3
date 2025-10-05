package cl.bancoxyz.analytics.client;

import cl.bancoxyz.analytics.dto.AnnualAccountDto;
import cl.bancoxyz.analytics.dto.TransactionDto;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "legacy-api", path = "/legacy")
public interface LegacyApiClient {

  @GetMapping("/transactions")
  List<TransactionDto> transactions(@RequestParam(value = "accountId", required = false) String accountId,
                                    @RequestParam(value = "from", required = false) String from,
                                    @RequestParam(value = "to", required = false) String to);

  @GetMapping("/annual")
  List<AnnualAccountDto> annual(@RequestParam(value = "accountId", required = false) String accountId,
                                @RequestParam(value = "year", required = false) String year);
}
