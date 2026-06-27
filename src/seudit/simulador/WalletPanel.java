package seudit.simulador;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

// Representa visualmente un monedero SE-UDIT simulado.
    // Contiene la pantalla, botones de operación y lógica de interacción del usuario.  
    class WalletPanel extends JPanel {
    	private static final long serialVersionUID = 1L; //evita un warning por no usar un identificador
    	// y heredar de la clase serializable JPanel
    	
    	private static final DecimalFormat MONEY = new DecimalFormat("0.00");

    	private static int NEXT_UDIT = 1;

    	private static String nextUditId() {
    	    return String.format("UDIT-%04d", NEXT_UDIT++);
    	}
    	
    	// Modelo interno del monedero (saldo, UDIT, historial y estados).
        final WalletModel model;
        
        // Referencia al otro monedero SE-UDIT de la simulación.
        WalletPanel peer;
        
        // Registro común donde se muestran eventos y fases de los protocolos.
        final JTextArea logger;
        
        // Pantalla principal del monedero.
        DisplayPanel display;

        JButton up, down, left, right, center,
        load, transfer, fragment, fusion, history,
        block, unblock;

        WalletPanel(WalletModel model, JTextArea logger) {
            this.model = model;
            this.logger = logger;
            build();
            refresh();
        }

        void build() {
            setLayout(new BorderLayout());
            setBackground(new Color(8, 31, 49));
            setBorder(new CompoundBorder(new LineBorder(new Color(82, 124, 151), 2), new EmptyBorder(14, 14, 14, 14)));

            JLabel title = label("Monedero digital offline SE-UDIT", 22, Color.WHITE);
            JLabel sub = label(model.name + " . " + model.id, 14, new Color(255, 214, 72));

            JPanel head = new JPanel(new GridLayout(2, 1));
            head.setOpaque(false);
            head.add(title);
            head.add(sub);
            add(head, BorderLayout.NORTH);

            JPanel body = new JPanel(new GridBagLayout());
            body.setOpaque(false);

            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(10, 18, 10, 18);

            gc.gridx = 0;
            gc.gridy = 0;
            gc.gridheight = 2;
            body.add(new ChipPanel(), gc);

            display = new DisplayPanel(model);
            display.setPreferredSize(new Dimension(300, 420));
            display.setMinimumSize(new Dimension(300, 420));
            display.setMaximumSize(new Dimension(300, 420));

            gc.gridx = 1;
            gc.fill = GridBagConstraints.NONE;
            gc.weightx = 0;
            gc.weighty = 0;
            body.add(display, gc);

            JPanel nav = new JPanel(new GridBagLayout());
            nav.setOpaque(false);

            up = navButton("▲");
            down = navButton("▼");
            left = navButton("◄");
            right = navButton("►");
center = navButton("✓");

            GridBagConstraints ac = new GridBagConstraints();
            ac.insets = new Insets(5, 5, 5, 5);

            ac.gridx = 1;
            ac.gridy = 0;
            nav.add(up, ac);

            ac.gridx = 0;
            ac.gridy = 1;
            nav.add(left, ac);

            ac.gridx = 1;
            nav.add(center, ac);

            ac.gridx = 2;
            nav.add(right, ac);

            ac.gridx = 1;
            ac.gridy = 2;
            nav.add(down, ac);

            gc.gridx = 2;
            gc.gridy = 0;	
            gc.gridheight = 1;
            body.add(nav, gc);

            JPanel acts = new JPanel(new GridLayout(7, 1, 0, 7));
            acts.setOpaque(false);

            load = act("Cargar");
            transfer = act("Transferir");
            fragment = act("Fragmentar");
            fusion = act("Fusión");
            history = act("Historial");
            block = act("Simular Error");
            unblock = act("Recuperación");

            for (JButton b : new JButton[]{
                    load,
                    transfer,
                    fragment,
                    fusion,
                    history,
                    block,
                    unblock}) {
                acts.add(b);
            }
            gc.gridx = 2;
            gc.gridy = 1;
            body.add(acts, gc);

            add(body, BorderLayout.CENTER);

            JLabel foot = label("SEGURO POR DISEÑO · VALOR BAJO TU CONTROL", 12, new Color(255, 214, 72));
            add(foot, BorderLayout.SOUTH);

            install();
        }

        JLabel label(String t, int size, Color c) {
            JLabel l = new JLabel(t, SwingConstants.CENTER);
            l.setForeground(c);
            l.setFont(new Font(Font.SANS_SERIF, Font.BOLD, size));
            return l;
        }

        JButton navButton(String t) {
            JButton b = new JButton(t);
            b.setPreferredSize(new Dimension(70, 50));
            b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 38));
            b.setForeground(Color.WHITE);
            b.setBackground(new Color(44, 57, 73));
            b.setFocusPainted(false);
            b.setBorder(new LineBorder(new Color(140, 160, 180)));
            return b;
        }

        JButton act(String t) {
            JButton b = new JButton(t);
            b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            b.setFocusPainted(false);
            return b;
        }

        void install() {
        	load.addActionListener(e -> doLoad());
        	transfer.addActionListener(e -> doTransfer());
        	fusion.addActionListener(e -> doFusion());
            
        	// Inicia la operación de fragmentación.
        	// El botón no fragmenta todavía: prepara la pantalla de entrada
        	// para que el usuario indique la cantidad a separar.
        	//La confirmación real se produce cuando se pulsa el botón central
        	//dentro del método pressCenter()
        	
            fragment.addActionListener(e -> {
                if (model.pendingOutgoing != null) {
                    flash("FRAGMENTO\nPENDIENTE");
                    return;
                }

                model.selectedAmountText = "";
                model.cursor = -1;
                display.mode = Mode.FRAGMENT_INPUT;
                log(model.name + " inicia fragmentación.");
                refresh();
            });

            history.addActionListener(e -> showHistory());
            
            
            // Simula un fallo durante una operación pendiente.
            // Se limpian los datos temporales de transferencia y se marca
            // que existe una recuperación pendiente, para validar la atomicidad
            // mediante el flujo ERROR -> ROLLBACK -> ESTADO_RESTAURADO.
            block.addActionListener(e -> {
            	model.pendingOutgoing = null;
            	model.amountConfirmed = false;
            	model.pendingOutgoingFromFragment = false;
            	model.pendingOutgoingUditId = null;
            	model.pendingOutgoingValue = null;
            	model.recoveryPending = true;
            	flash("ERROR");
                model.history.add(new HistoryEntry("ERROR simulado durante operación"));
                log(model.name + " simula ERROR durante operación.");
                refresh();
            });
            
            
            // Ejecuta la recuperación lógica del estado tras un fallo.
            // Representa el rollback de la operación y deja el monedero
            // en un estado restaurado y consistente.
            unblock.addActionListener(e -> {
            	model.recoveryPending = false;
            	flash("ROLLBACK\nESTADO\nRESTAURADO");
                model.history.add(new HistoryEntry("ROLLBACK ejecutado"));
                model.history.add(new HistoryEntry("ESTADO RESTAURADO"));
                log(model.name + " ejecuta ROLLBACK. Estado restaurado.");
                refresh();
            });

            center.addActionListener(e -> pressCenter());
            up.addActionListener(e -> pressUp());
            down.addActionListener(e -> pressDown());
            left.addActionListener(e -> pressLeft());

            final long[] t = {0};

            right.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    t[0] = System.currentTimeMillis();
                }

                public void mouseReleased(MouseEvent e) {
                    if (System.currentTimeMillis() - t[0] >= 650) {
                        pressRightLong();
                    } else {
                        pressRightShort();
                    }
                }
            });
        }
        
        // Crea la primera UDIT del monedero o recibe una transferencia pendiente.
        void doLoad() {
            if (blocked()) return;
            
            if (peer != null && peer.model.recoveryPending) {
                flash("ERROR\nOPERACIÓN\nNO COMPLETADA");
                log(model.name + " detecta operación fallida. Use Recuperación para restaurar el estado.");
                refresh();
                return;
            }

            if (peer == null || peer.model.pendingOutgoing == null) {
                model.selectedAmountText = "";
                model.cursor = -1;
                display.mode = Mode.INITIAL_LOAD_INPUT;
                log(model.name + " inicia carga inicial autorizada.");
                refresh();
                return;
            }

            WalletModel src = peer.model;
            double amount = src.pendingOutgoing;

            if (src.blocked) {
                flash("EMISOR\nBLOQUEADO");
                log("Transferencia cancelada: emisor bloqueado.");
                return;
            }

            if (amount <= 0) {
                flash("ERROR\nSALDO");
                src.pendingOutgoing = null;
                peer.refresh();
                return;
            }

            if (!src.pendingOutgoingFromFragment && src.balance < amount) {
                flash("ERROR\nSALDO");
                src.pendingOutgoing = null;
                peer.refresh();
                return;
            }

            String op = "OP_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            log("Proximidad detectada entre " + src.name + " y " + model.name + ".");
            log("Fase 1. Inicialización " + op + ".");
            log("Fase 2. Autenticación mutua.");
            log("Fase 3. Validación local en emisor.");
            log("Fase 4. Envío de mensaje firmado.");
            log("Fase 5. Validación local en receptor.");
            log("Fase 6. Confirmación firmada.");
            log("Fase 7. UDIT original marcada como GASTADA.");

            String spentUdit = src.activeUditId == null ? "UDIT-SIN-ID" : src.activeUditId;
            String receivedUdit = model.nextUditId();
            String changeUdit = null;

            double remaining = Math.round((src.balance - amount) * 100.0) / 100.0;
            if (remaining > 0) changeUdit = nextUditId();

            String previousReceiverUdit = model.activeUditId;
            String fusedReceiverUdit = null;
            boolean receiverHadPreviousBalance = model.initialized && model.balance > 0 && previousReceiverUdit != null;

            if (receiverHadPreviousBalance) fusedReceiverUdit = model.nextUditId();

            if (!src.pendingOutgoingFromFragment) {
                src.balance -= amount;
            }
            model.balance += amount;

            src.initialized = true;
            model.initialized = true;

            src.activeUditId = changeUdit;
            model.activeUditId = receiverHadPreviousBalance ? fusedReceiverUdit : receivedUdit;

            if (src.pendingOutgoingFromFragment && src.pendingOutgoingUditId != null) {
                src.udits.removeIf(u -> u.id.equals(src.pendingOutgoingUditId));

                model.udits.add(
                    new Udit(
                        receivedUdit,
                        amount,
                        "ACTIVA",
                        "transferencia desde " + src.name,
                        model.id
                    )
                );

                src.history.add(new HistoryEntry(src.pendingOutgoingUditId + " GASTADA por transferencia"));
            } else {
                src.history.add(new HistoryEntry(spentUdit + " GASTADA"));
            }
            
            src.history.add(new HistoryEntry("Transferido: -" + money(amount) + " a " + model.name));

            if (src.udits.isEmpty()) {
                src.history.add(new HistoryEntry("Sin UDIT activa restante"));
            }

            src.history.add(new HistoryEntry("Saldo: " + src.balanceText()));

            model.history.add(new HistoryEntry(receivedUdit + " creada: " + money(amount)));
            model.history.add(new HistoryEntry("Origen de " + receivedUdit + ": transferencia desde " + src.name));
            model.history.add(new HistoryEntry("Valor recibido: +" + money(amount)));

            if (receiverHadPreviousBalance) {
                model.history.add(new HistoryEntry(previousReceiverUdit + " fusionada con " + receivedUdit));
                model.history.add(new HistoryEntry(fusedReceiverUdit + " ACTIVA: " + model.balanceText()));
            } else {
                model.history.add(new HistoryEntry(receivedUdit + " ACTIVA: " + money(amount)));
            }

            model.history.add(new HistoryEntry("Saldo: " + model.balanceText()));

            src.pendingOutgoing = null;
            src.amountConfirmed = false;
            src.selectedAmountText = "";
            src.cursor = -1;

            log("Transferencia completada: " + money(amount) + ".");
            peer.refresh();
            refresh();
        }
        
        // Prepara una transferencia offline desde este monedero hacia el monedero asociado.
        void doTransfer() {
            if (blocked()) return;

            if (!model.accessGranted) {
                flash("ACCESO\nREQUERIDO");
                return;
            }

            if (!model.initialized) { flash("SALDO\nVACÍO"); return; }

            if (model.udits.size() > 1) {
                model.selectedUditIndex = 0;
                display.mode = Mode.SELECT_UDIT_TRANSFER;
                log(model.name + " inicia selección de UDIT para transferencia.");
                refresh();
                return;
            }

            if (model.pendingOutgoing != null && model.amountConfirmed) {
                log(model.name + " deja preparada transferencia de " + money(model.pendingOutgoing) + ". El receptor debe pulsar Cargar.");
                flash("TRANSFERENCIA\nPREPARADA");
                refresh();
                return;
            }

            if (!model.amountConfirmed) {
                model.selectedAmountText = "";
                model.cursor = -1;
                display.mode = Mode.TRANSFER_INPUT;
                log(model.name + " inicia selección de importe.");
                refresh();
                return;
            }

            double amount = parse(model.selectedAmountText);
            if (amount <= 0) { flash("IMPORTE\nNO VÁLIDO"); return; }
            if (amount > model.balance) { flash("SALDO\nINSUFICIENTE"); return; }

            model.pendingOutgoing = amount;
            log(model.name + " deja preparada transferencia de " + money(amount) + ". El receptor debe pulsar Cargar.");
            flash("TRANSFERENCIA\nPREPARADA");
            refresh();
        }
        
        // Fusiona varias UDIT activas en una única nueva UDIT, conservando el valor total.
        void doFusion() {

            if (model.udits.size() < 2) {
                flash("NO\nFUSIÓN");
                log(model.name + " intenta fusionar con menos de 2 UDIT activas.");
                refresh();
                return;
            }

            double total = 0.0;

            for (Udit u : model.udits) {
                total += u.value;
            }

            total = Math.round(total * 100.0) / 100.0;

            for (Udit u : model.udits) {
                model.history.add(
                    new HistoryEntry(
                    		u.id + " GASTADA por fusión - " + money(u.value)
                    )
                );
            }
            
            int fusedCount = model.udits.size();
            String newUditId = model.nextUditId();

            model.udits.clear();
            model.udits.add(
                new Udit(
                    newUditId,
                    total,
                    "ACTIVA",
                    "fusión",
                    model.id
                )
            );

            model.balance = total;
            model.activeUditId = newUditId;
            model.initialized = true;

            model.history.add(
                new HistoryEntry(
                    newUditId + " ACTIVA: " + money(total)
                )
            );

            model.history.add(
                new HistoryEntry(
                    "Saldo: " + model.balanceText()
                )
            );

            flash("FUSIÓN\n" + money(total));

            log(model.name + " fusiona " + fusedCount
                + " UDIT en " + newUditId
                + " por un total de " + money(total) + ".");

            refresh();
        }

        // Muestra el historial navegable de operaciones realizadas sobre las UDIT.
        void showHistory() {
            if (blocked()) return;
            display.mode = Mode.HISTORY;
            model.historyIndex = Math.max(0, model.history.size() - 1);
            refresh();
        }

        void pressCenter() {
            if (blocked()) return;
            
            if (display.mode == Mode.SELECT_UDIT_TRANSFER) {
                if (model.udits.isEmpty()) {
                    flash("SIN\nUDIT");
                    return;
                }

                Udit selected = model.udits.get(model.selectedUditIndex);

                model.pendingOutgoing = selected.value;
                model.pendingOutgoingValue = selected.value;
                model.pendingOutgoingFromFragment = true;
                model.pendingOutgoingUditId = selected.id;
                model.amountConfirmed = true;

                display.mode = Mode.NORMAL;

                flash("TRANSFERENCIA\nPREPARADA");
                log(model.name + " selecciona " + selected.id + " para transferir: " + money(selected.value));

                refresh();
                return;
            }

            if (display.mode == Mode.INITIAL_LOAD_INPUT) {
                double a = parse(model.selectedAmountText);
                if (a <= 0) { flash("IMPORTE\nNO VÁLIDO"); return; }

                model.balance = a;
                model.initialized = true;
                model.activeUditId = model.nextUditId();

                model.udits.clear();
                model.udits.add(new Udit(model.activeUditId, a, "ACTIVA", "carga inicial", model.id));

                model.history.add(new HistoryEntry("Creada " + model.activeUditId));
                model.history.add(new HistoryEntry("Primera carga: " + money(a)));
                model.history.add(new HistoryEntry(model.activeUditId + " ACTIVA"));
                model.history.add(new HistoryEntry("Saldo: " + model.balanceText()));

                model.selectedAmountText = "";
                model.cursor = -1;
                display.mode = Mode.NORMAL;

                log(model.name + " primera carga: " + money(a));
                refresh();
                return;
            }       

            if (display.mode == Mode.TRANSFER_INPUT) {
                double a = parse(model.selectedAmountText);
                if (a <= 0) { flash("IMPORTE\nNO VÁLIDO"); return; }

                model.amountConfirmed = true;
                display.mode = Mode.NORMAL;

                log(model.name + " confirma importe: " + money(a));
                refresh();
                return;
            }
            
            // Confirmación de la fragmentación introducida por el usuario.
            // La UDIT original pasa a GASTADA y se crean dos nuevas UDIT ACTIVAS,
            // conservando el valor total.
            if (display.mode == Mode.FRAGMENT_INPUT) {
                double a = parse(model.selectedAmountText);

                if (a <= 0) { flash("IMPORTE\nNO VÁLIDO"); return; }
                if (a >= model.balance) { flash("IMPORTE\nNO VÁLIDO"); return; }

                double restante = Math.round((model.balance - a) * 100.0) / 100.0;

                String uditOriginal = model.activeUditId == null ? "UDIT-SIN-ID" : model.activeUditId;
                String uditRestante = model.nextUditId();
                String uditFragmento = model.nextUditId();

                model.balance = restante;
                model.activeUditId = uditRestante;
                
                model.udits.clear();
                model.udits.add(new Udit(uditRestante, restante, "ACTIVA", "fragmentación", model.id));
                model.udits.add(new Udit(uditFragmento, a, "PENDIENTE_TRANSFERENCIA", "fragmentación", model.id));

                model.pendingOutgoing = a;
                model.pendingOutgoingValue = a;
                model.pendingOutgoingFromFragment = true;
                model.pendingOutgoingUditId = uditFragmento;
                model.amountConfirmed = true;

                model.history.add(new HistoryEntry(uditOriginal + " GASTADA"));
                model.history.add(new HistoryEntry(uditRestante + " ACTIVA: " + money(restante)));
                model.history.add(new HistoryEntry(uditFragmento + " ACTIVA: " + money(a)));
                model.history.add(new HistoryEntry(uditFragmento + " PENDIENTE DE TRANSFERENCIA: " + money(a)));

                model.selectedAmountText = "";
                model.cursor = -1;
                display.mode = Mode.NORMAL;

                flash("FRAGMENTADA\n" + money(a));
                log(model.name + " fragmenta " + money(a) + ". " + uditOriginal + " GASTADA; " + uditRestante + " ACTIVA; " + uditFragmento + " ACTIVA seleccionada.");

                refresh();
                return;
            }

            if (display.mode == Mode.HISTORY) {
                display.mode = Mode.NORMAL;
                refresh();
                return;
            }

            if (!model.accessGranted) {
                if (model.selectedAmountText.equals("1234")) {
                    model.selectedAmountText = "";
                    model.cursor = -1;
                    model.failedPinAttempts = 0;
                    model.accessGranted = true;
                    flash("PIN\nCORRECTO");
                    log(model.name + " acceso concedido con PIN correcto.");
                    return;
                }

                if (model.selectedAmountText.equals("9999")) {
                    model.selectedAmountText = "";
                    model.cursor = -1;
                    model.failedPinAttempts = 0;
                    model.accessGranted = true;
                    String ts = new java.text.SimpleDateFormat("HH:mm:ss").format(new Date());
                    flash("ALARMA\n" + ts);
                    log(model.name + " acceso con PIN de coacción. Wearable genera alarma protegida con timestamp " + ts + ".");
                    return;
                }

                if (model.selectedAmountText.length() == 4) {
                    model.failedPinAttempts++;

                    if (model.failedPinAttempts >= 4) {
                        model.selectedAmountText = "";
                        model.cursor = -1;
                        model.blockedUntil = System.currentTimeMillis() + 60000;
                        flash("BLOQUEADO\n24 h");
                        log(model.name + " bloqueado tras 4 PIN incorrectos. (Demo: 60 segundos)");
                        return;
                    }

                    model.selectedAmountText = "";
                    model.cursor = -1;
                    flash("PIN\nINCORRECTO");
                    log(model.name + " PIN incorrecto. Intentos restantes: " + (4 - model.failedPinAttempts));
                    return;
                }

                return;
            }
        }

        void pressUp() {
        	
        	if (display.mode == Mode.HISTORY) {
        	    model.northResetCount = 0;
        	    model.historyIndex = Math.max(0, model.historyIndex - 1);
        	    refresh();
        	    return;
        	}
        	
        	if (display.mode == Mode.SELECT_UDIT_TRANSFER) {
        	    if (!model.udits.isEmpty()) {
        	        model.selectedUditIndex =
        	            (model.selectedUditIndex - 1 + model.udits.size()) % model.udits.size();
        	        refresh();
        	    }
        	    return;
        	}

        	if (display.mode == Mode.INITIAL_LOAD_INPUT ||
        		    display.mode == Mode.TRANSFER_INPUT ||
        		    display.mode == Mode.FRAGMENT_INPUT) {
                model.northResetCount = 0;
                if (model.selectedAmountText.isEmpty()) return;
                if (model.cursor < 0) model.cursor = model.selectedAmountText.length() - 1;
                edit(+1);
                return;
            }

            if (System.currentTimeMillis() < model.blockedUntil) {
                model.northResetCount++;

                if (model.northResetCount >= 4) {
                    model.blockedUntil = 0;
                    model.failedPinAttempts = 0;
                    model.northResetCount = 0;
                    model.selectedAmountText = "";
                    model.cursor = -1;

                    flash("REINICIO\nDEMO");
                    log(model.name + " reinicio manual de demo mediante 4 pulsaciones Norte.");
                    return;
                }

                flash("BLOQUEADO\n24 h");
                return;
            }

            if (model.accessGranted) {
                model.northResetCount++;

                if (model.northResetCount >= 4) {
                    model.accessGranted = false;
                    model.selectedAmountText = "";
                    model.cursor = -1;
                    model.northResetCount = 0;
                    flash("REINICIO\nDEMO");
                    log(model.name + " vuelve a la pantalla de PIN mediante 4 pulsaciones Norte.");
                    return;
                }

                flash("PULSE NORTE\n" + model.northResetCount + "/4");
                return;
            }

            model.northResetCount = 0;

            if (model.selectedAmountText.isEmpty()) return;
            if (model.cursor < 0) model.cursor = model.selectedAmountText.length() - 1;
            edit(+1);
        }

        void pressDown() {

            if (display.mode == Mode.HISTORY) {
                model.northResetCount = 0;
                model.historyIndex = Math.min(model.history.size() - 1, model.historyIndex + 1);
                refresh();
                return;
            }

            if (display.mode == Mode.SELECT_UDIT_TRANSFER) {
                if (!model.udits.isEmpty()) {
                    model.selectedUditIndex =
                        (model.selectedUditIndex + 1) % model.udits.size();
                    refresh();
                }
                return;
            }

            if (model.selectedAmountText.isEmpty()) return;
            if (model.cursor < 0) model.cursor = model.selectedAmountText.length() - 1;
            edit(-1);
        }

        void pressLeft() {
            if (!editing() || model.selectedAmountText.isEmpty()) return;
            model.cursor = (model.cursor <= 0) ? model.selectedAmountText.length() - 1 : model.cursor - 1;
            refresh();
        }

        void pressRightShort() {
            if (blocked()) return;

            if (model.selectedAmountText.length() >= 4) return;
            model.selectedAmountText += "0";
            model.cursor = model.selectedAmountText.length() - 1;
            refresh();
        }
        void pressRightLong() {
            // En pantalla PIN no usamos coma decimal.
        }

        boolean editing() {
            return true;
        }
        void add(char c) {
            if (model.selectedAmountText.length() >= 10) return;
            if (model.cursor < 0 || model.cursor >= model.selectedAmountText.length() - 1) {
                model.selectedAmountText += c;
                model.cursor = model.selectedAmountText.length() - 1;
            } else {
                int i = model.cursor + 1;
                model.selectedAmountText = model.selectedAmountText.substring(0, i) + c + model.selectedAmountText.substring(i);
                model.cursor = i;
            }
            refresh();
        }

        void edit(int d) {
            if (!editing() || model.selectedAmountText.isEmpty()) return;
            if (model.cursor < 0) model.cursor = model.selectedAmountText.length() - 1;
            char c = model.selectedAmountText.charAt(model.cursor);
            if (c == ',') return;
            int n = (c - '0' + d + 10) % 10;
            StringBuilder sb = new StringBuilder(model.selectedAmountText);
            sb.setCharAt(model.cursor, (char) ('0' + n));
            model.selectedAmountText = sb.toString();
            refresh();
        }

        boolean blocked() {

            if (System.currentTimeMillis() < model.blockedUntil) {
                flash("BLOQUEADO\n24 h");
                return true;
            }

            if (model.blockedUntil != 0 &&
                System.currentTimeMillis() >= model.blockedUntil) {

                model.blockedUntil = 0;
                model.failedPinAttempts = 0;

                flash("FIN\nBLOQUEO");
                log(model.name + " fin del bloqueo temporal.");
            }

            if (model.blocked) {
                flash("BLOQUEADO");
                log(model.name + " rechaza operación: bloqueado.");
                return true;
            }

            return false;
        }

        double parse(String s) {
            try {
                if (s == null || s.trim().isEmpty()) return 0;
                String c = s.replace(",", ".");
                if (c.equals(".") || c.endsWith(".")) return 0;
                return Math.round(Double.parseDouble(c) * 100.0) / 100.0;
            } catch (Exception e) { return 0; }
        }

        String money(double v) { return MONEY.format(v).replace(".", ",") + " UDIT"; }
        void flash(String s) { display.flashMessage(s); }

        void log(String s) {
            logger.append("[" + new java.text.SimpleDateFormat("HH:mm:ss").format(new Date()) + "] " + s + "\n");
            logger.setCaretPosition(logger.getDocument().getLength());
        }

        void refresh() {
            display.repaint();
            block.setEnabled(true);
            unblock.setEnabled(true);
        }
    }