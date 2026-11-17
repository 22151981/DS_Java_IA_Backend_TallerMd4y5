package com.devsenior.sillon.cita;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class EstadoCitaTest {
    static Stream<Arguments> permitidas() {
        return Stream.of(Arguments.of(EstadoCita.PENDIENTE, EstadoCita.CONFIRMADA), Arguments.of(EstadoCita.PENDIENTE, EstadoCita.CANCELADA), Arguments.of(EstadoCita.CONFIRMADA, EstadoCita.COMPLETADA), Arguments.of(EstadoCita.CONFIRMADA, EstadoCita.CANCELADA), Arguments.of(EstadoCita.CONFIRMADA, EstadoCita.NO_ASISTIO));
    }
    @ParameterizedTest @MethodSource("permitidas")
    void aceptaTransicionesPermitidas(EstadoCita origen, EstadoCita destino) { assertTrue(origen.puedeTransicionarA(destino)); }
    static Stream<Arguments> bloqueadas() {
        return Stream.of(Arguments.of(EstadoCita.CONFIRMADA, EstadoCita.PENDIENTE), Arguments.of(EstadoCita.COMPLETADA, EstadoCita.CANCELADA), Arguments.of(EstadoCita.CANCELADA, EstadoCita.CONFIRMADA), Arguments.of(EstadoCita.NO_ASISTIO, EstadoCita.COMPLETADA), Arguments.of(EstadoCita.COMPLETADA, EstadoCita.PENDIENTE), Arguments.of(EstadoCita.CANCELADA, EstadoCita.PENDIENTE), Arguments.of(EstadoCita.PENDIENTE, EstadoCita.COMPLETADA));
    }
    @ParameterizedTest @MethodSource("bloqueadas")
    void bloqueaTransicionesInvalidas(EstadoCita origen, EstadoCita destino) { assertFalse(origen.puedeTransicionarA(destino)); }
    @Test void destinoNuloEsInvalido() { assertFalse(EstadoCita.PENDIENTE.puedeTransicionarA(null)); }
}
