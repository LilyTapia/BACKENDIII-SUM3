package cl.bancoxyz.bff.legacyapi;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class LegacyApiApplication {
  public static void main(String[] args) {
    SpringApplication.run(LegacyApiApplication.class, args);
  }
}
