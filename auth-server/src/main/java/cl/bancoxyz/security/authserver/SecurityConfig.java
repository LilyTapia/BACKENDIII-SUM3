package cl.bancoxyz.security.authserver;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.proc.SecurityContext;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.*;
import com.nimbusds.jose.jwk.source.JWKSource;
import org.springframework.security.oauth2.server.authorization.client.*;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
public class SecurityConfig {

  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE)
  public SecurityFilterChain wellKnownFilterChain(HttpSecurity http) throws Exception {
    // Exponemos los endpoints de descubrimiento (jwks, metadata) sin autenticación
    http.securityMatcher("/.well-known/**")
        .authorizeHttpRequests(a -> a.anyRequest().permitAll())
        .csrf(AbstractHttpConfigurer::disable);
    return http.build();
  }

  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE + 1)
  public SecurityFilterChain authServerSecurityFilterChain(HttpSecurity http) throws Exception {
    // Configuración estándar del Authorization Server (endpoints /oauth2/
    // y emisión de tokens firmados con el JWKS generado en memoria)
    OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
    http.exceptionHandling(ex -> ex.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")));
    http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
    return http.build();
  }

  @Bean
  public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(a -> a
        .requestMatchers("/.well-known/**", "/oauth2/token").permitAll()
        .anyRequest().authenticated());
    // Se expone httpBasic para validar rápidamente al administrador (auth-admin)
    http.httpBasic(Customizer.withDefaults());
    return http.build();
  }

  @Bean
  public RegisteredClientRepository registeredClientRepository(PasswordEncoder passwordEncoder) {
    // Clientes machine-to-machine para cada canal; se agregan scopes
    // específicos para poder restringir llamadas en los BFF
    RegisteredClient webClient = RegisteredClient.withId(UUID.randomUUID().toString())
        .clientId("bff-web-client")
        .clientSecret(passwordEncoder.encode("bff-web-secret"))
        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
        .scope("bff.web.read")
        .scope("bff.web.write")
        .scope("bff.web.admin")
        .tokenSettings(defaultTokenSettings())
        .build();

    RegisteredClient mobileClient = RegisteredClient.withId(UUID.randomUUID().toString())
        .clientId("bff-mobile-client")
        .clientSecret(passwordEncoder.encode("bff-mobile-secret"))
        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
        .scope("bff.mobile.read")
        .scope("bff.mobile.admin")
        .tokenSettings(defaultTokenSettings())
        .build();

    RegisteredClient atmClient = RegisteredClient.withId(UUID.randomUUID().toString())
        .clientId("bff-atm-client")
        .clientSecret(passwordEncoder.encode("bff-atm-secret"))
        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
        .scope("bff.atm.read")
        .scope("bff.atm.write")
        .scope("bff.atm.admin")
        .tokenSettings(defaultTokenSettings())
        .build();

    return new InMemoryRegisteredClientRepository(webClient, mobileClient, atmClient);
  }

  private TokenSettings defaultTokenSettings() {
    return TokenSettings.builder()
        .accessTokenTimeToLive(Duration.ofHours(2))
        .reuseRefreshTokens(false)
        .build();
  }

  @Bean
  public AuthorizationServerSettings authorizationServerSettings(
      @Value("${auth-server.issuer:http://localhost:9000}") String issuer) {
    return AuthorizationServerSettings.builder().issuer(issuer).build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Bean
  public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
    UserDetails admin = User.withUsername("auth-admin")
        .password(passwordEncoder.encode("auth-admin-123"))
        .roles("ADMIN")
        .build();
    return new InMemoryUserDetailsManager(admin);
  }

  @Bean
  public JWKSource<SecurityContext> jwkSource() {
    RSAKey rsaKey = Jwks.generateRsa();
    JWKSet jwkSet = new JWKSet(rsaKey);
    return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
  }

  @Bean
  public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
    return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
  }
}
