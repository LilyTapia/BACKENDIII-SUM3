package cl.bancoxyz.bff.bffatm.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {
  private final Set<String> validTokens = ConcurrentHashMap.newKeySet();

  public String issueToken(String card) {
    String token = UUID.randomUUID().toString();
    validTokens.add(token);
    return token;
  }

  public boolean isValid(String token) {
    return token != null && validTokens.contains(token);
  }

  public void revoke(String token) {
    if (token != null) validTokens.remove(token);
  }
}

