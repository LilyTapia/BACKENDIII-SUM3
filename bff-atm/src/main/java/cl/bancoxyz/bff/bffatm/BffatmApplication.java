package cl.bancoxyz.bff.bffatm;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class BffatmApplication {
  public static void main(String[] args) {
    SpringApplication.run(BffatmApplication.class, args);
  }
}