package com.devsenior.sillon.pago;

import com.stripe.StripeClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    @Bean
    public StripeClient stripeClient(
            @Value("${stripe.clave.secreta}") String claveSecreta,
            @Value("${stripe.url.api}") String urlApi) {
        return StripeClient.builder()
                .setApiKey(claveSecreta)
                .setApiBase(urlApi)
                .build();
    }
}
