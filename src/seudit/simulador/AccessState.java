package seudit.simulador;

/**
 * Estados de acceso del monedero SE-UDIT simulado.
 * 
 * Representa el estado de autenticación del usuario y las situaciones
 * especiales de operación normal, coacción o bloqueo por PIN.
 */
enum AccessState {
    WAITING_PIN,
    OPERATIVE_NORMAL,
    OPERATIVE_DURESS,
    BLOCKED_BY_PIN
}