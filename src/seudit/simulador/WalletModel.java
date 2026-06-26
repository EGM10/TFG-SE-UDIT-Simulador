package seudit.simulador;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Modelo interno de un monedero SE-UDIT simulado.
 * 
 * Almacena el saldo, las unidades UDIT, el historial de operaciones,
 * los estados de autenticación, bloqueo, transferencia pendiente y recuperación.
 */
class WalletModel {

    private static final DecimalFormat MONEY = new DecimalFormat("0.00");

    final String name;
    final String id;

    double balance = 0.0;

    boolean initialized = false;
    boolean blocked = false;
    boolean amountConfirmed = false;
    boolean recoveryPending = false;

    String activeUditId = null;
    String selectedAmountText = "";
    String pendingFrom = null;

    int cursor = -1;
    int failedPinAttempts = 0;
    long blockedUntil = 0;
    int northResetCount = 0;
    boolean accessGranted = false;
    int historyIndex = 0;
    int selectedUditIndex = 0;

    Double pendingOutgoing = null;
    boolean pendingOutgoingFromFragment = false;
    String pendingOutgoingUditId = null;
    Double pendingOutgoingValue = null;

    final List<Udit> udits = new ArrayList<>();
    final List<HistoryEntry> history = new ArrayList<>();

    int nextUditNumber = 1;

    WalletModel(String name) {
        this.name = name;
        this.id = "ID_" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        history.add(new HistoryEntry("Saldo inicial: Vacío"));
    }

    String nextUditId() {
        return String.format("UDIT-%04d", nextUditNumber++);
    }

    String balanceText() {
        return !initialized ? "Vacío" : MONEY.format(balance).replace(".", ",") + " UDIT";
    }
}