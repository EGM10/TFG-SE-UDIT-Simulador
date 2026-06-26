package seudit.simulador;

import java.text.DecimalFormat;

// Representa una unidad UDIT dentro de la simulación.
// Cada UDIT tiene identificador, valor, estado funcional y origen.
class Udit {

    private static final DecimalFormat MONEY = new DecimalFormat("0.00");

    final String id;
    final double value;
    String state;
    final String origin;
    final String walletId;

    Udit(String id, double value, String state, String origin, String walletId) {
        this.id = id;
        this.value = value;
        this.state = state;
        this.origin = origin;
        this.walletId = walletId;
    }

    String shortText() {
        return id + " " + state + ": " + MONEY.format(value).replace(".", ",") + " UDIT";
    }
}