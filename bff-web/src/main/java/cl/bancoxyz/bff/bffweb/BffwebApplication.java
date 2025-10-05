package cl.bancoxyz.bff.bffweb;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
public class BffwebApplication {
  public static void main(String[] args) {
    SpringApplication.run(BffwebApplication.class, args);
  }
}
