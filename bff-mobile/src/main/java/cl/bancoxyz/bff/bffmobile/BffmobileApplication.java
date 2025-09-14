package cl.bancoxyz.bff.bffmobile;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class BffmobileApplication {
  public static void main(String[] args) {
    SpringApplication.run(BffmobileApplication.class, args);
  }
}