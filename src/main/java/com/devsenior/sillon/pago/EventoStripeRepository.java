package com.devsenior.sillon.pago;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventoStripeRepository extends JpaRepository<EventoStripe, String> {
}
