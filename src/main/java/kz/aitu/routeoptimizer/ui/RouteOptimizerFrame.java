package kz.aitu.routeoptimizer.ui;

import kz.aitu.routeoptimizer.demo.DemoData;
import kz.aitu.routeoptimizer.domain.DeliveryPriority;
import kz.aitu.routeoptimizer.domain.DeliveryRequest;
import kz.aitu.routeoptimizer.domain.Route;
import kz.aitu.routeoptimizer.selector.StrategySelector;
import kz.aitu.routeoptimizer.service.NoSuitableRouteException;
import kz.aitu.routeoptimizer.service.RouteDecision;
import kz.aitu.routeoptimizer.service.RouteMetrics;
import kz.aitu.routeoptimizer.service.RoutePlanner;
import kz.aitu.routeoptimizer.service.StrategyComparisonService;
import kz.aitu.routeoptimizer.strategy.BalancedRouteStrategy;
import kz.aitu.routeoptimizer.strategy.CheapestRouteStrategy;
import kz.aitu.routeoptimizer.strategy.EcoRouteStrategy;
import kz.aitu.routeoptimizer.strategy.FastestRouteStrategy;
import kz.aitu.routeoptimizer.strategy.RouteStrategy;
import kz.aitu.routeoptimizer.strategy.SafestRouteStrategy;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

final class RouteOptimizerFrame extends JFrame {

    private static final Color ACCENT = new Color(24, 103, 192);
    private static final Color SUCCESS = new Color(26, 127, 77);
    private static final Color ERROR = new Color(180, 45, 45);

    private final List<Route> routes = DemoData.routes();
    private final StrategySelector selector = StrategySelector.defaultSelector();

    private final JTextField requestIdField = new JTextField("REQ-GUI-001", 16);
    private final JComboBox<DeliveryPriority> priorityBox =
            new JComboBox<>(DeliveryPriority.values());
    private final JTextField budgetField = new JTextField("3000", 16);
    private final JTextField deadlineField = new JTextField("90", 16);
    private final JTextField emissionField = new JTextField("10", 16);
    private final JCheckBox ecoPreferredBox = new JCheckBox("Prefer an eco-friendly route");
    private final JComboBox<StrategyOption> strategyBox =
            new JComboBox<>(StrategyOption.values());

    private final DefaultTableModel routeModel = createRouteModel();
    private final JTable routeTable = new JTable(routeModel);
    private final JTextArea resultArea = new JTextArea();
    private final JLabel statusLabel = new JLabel("Ready", SwingConstants.LEFT);

    RouteOptimizerFrame() {
        super("Smart Delivery Route Optimizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 700));
        setSize(1220, 790);
        setLocationRelativeTo(null);

        setContentPane(buildContent());
        configureTable();
        configureResultArea();
        populateRoutes();
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(14, 14));
        root.setBorder(new EmptyBorder(16, 16, 12, 16));
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildInputPanel(), BorderLayout.WEST);
        root.add(buildWorkspace(), BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);
        return root;
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Smart Delivery and Route Optimization Engine");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 23f));
        title.setForeground(ACCENT);

        JLabel subtitle = new JLabel(
                "Configure a delivery request, select an optimization strategy, and compare routes."
        );
        subtitle.setFont(subtitle.getFont().deriveFont(13f));

        panel.add(title);
        panel.add(Box.createVerticalStrut(4));
        panel.add(subtitle);
        return panel;
    }

    private JPanel buildInputPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setPreferredSize(new Dimension(305, 0));
        wrapper.setBorder(BorderFactory.createTitledBorder("Delivery request"));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 9, 3, 9);

        addField(form, constraints, "Request ID", requestIdField);
        addField(form, constraints, "Priority", priorityBox);
        addField(form, constraints, "Maximum budget", budgetField);
        addField(form, constraints, "Deadline, minutes", deadlineField);
        addField(form, constraints, "Emission limit, kg", emissionField);

        constraints.insets = new Insets(9, 9, 5, 9);
        form.add(ecoPreferredBox, constraints);
        constraints.gridy++;

        constraints.insets = new Insets(5, 9, 3, 9);
        addField(form, constraints, "Strategy", strategyBox);

        JButton optimizeButton = primaryButton("Find best route");
        optimizeButton.setToolTipText("Run the selected strategy for this request");
        optimizeButton.addActionListener(event -> optimize());

        JButton compareButton = new JButton("Compare all strategies");
        compareButton.setToolTipText("Run all five strategies using the same request");
        compareButton.addActionListener(event -> compareAll());

        constraints.insets = new Insets(18, 9, 5, 9);
        form.add(optimizeButton, constraints);
        constraints.gridy++;
        constraints.insets = new Insets(3, 9, 9, 9);
        form.add(compareButton, constraints);
        constraints.gridy++;
        constraints.weighty = 1.0;
        form.add(Box.createVerticalGlue(), constraints);

        wrapper.add(form, BorderLayout.CENTER);
        return wrapper;
    }

    private void addField(
            JPanel panel,
            GridBagConstraints constraints,
            String labelText,
            Component component
    ) {
        JLabel label = new JLabel(labelText);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
        panel.add(label, constraints);
        constraints.gridy++;

        panel.add(component, constraints);
        constraints.gridy++;
    }

    private JSplitPane buildWorkspace() {
        JScrollPane tableScroll = new JScrollPane(routeTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Candidate routes"));

        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultScroll.setBorder(BorderFactory.createTitledBorder("Decision details"));

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT, tableScroll, resultScroll
        );
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(315);
        return splitPane;
    }

    private JPanel buildStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(2, 4, 0, 4));
        statusLabel.setForeground(SUCCESS);
        panel.add(statusLabel, BorderLayout.WEST);
        panel.add(new JLabel(routes.size() + " candidate routes"), BorderLayout.EAST);
        return panel;
    }

    private JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(button.getFont().deriveFont(Font.BOLD));
        button.setForeground(Color.WHITE);
        button.setBackground(ACCENT);
        button.setOpaque(true);
        return button;
    }

    private void configureTable() {
        routeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        routeTable.setAutoCreateRowSorter(true);
        routeTable.setFillsViewportHeight(true);
        routeTable.setRowHeight(25);
        routeTable.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer centered = new DefaultTableCellRenderer();
        centered.setHorizontalAlignment(SwingConstants.CENTER);
        TableColumnModel columns = routeTable.getColumnModel();
        for (int index = 1; index < columns.getColumnCount(); index++) {
            columns.getColumn(index).setCellRenderer(centered);
        }
        columns.getColumn(0).setPreferredWidth(125);
    }

    private void configureResultArea() {
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        resultArea.setMargin(new Insets(8, 8, 8, 8));
        resultArea.setText(
                "Enter request constraints and click 'Find best route'.\n"
                        + "Automatic mode maps the request priority to the appropriate strategy."
        );
    }

    private void populateRoutes() {
        for (Route route : routes) {
            routeModel.addRow(new Object[]{
                    route.id(),
                    format(route.distanceKm()),
                    route.estimatedMinutes(),
                    route.trafficLevel(),
                    route.weatherCondition(),
                    format(route.carbonEmissionKg()),
                    format(route.safetyRating()),
                    route.available() ? "Yes" : "No"
            });
        }
    }

    private void optimize() {
        try {
            DeliveryRequest request = readRequest();
            StrategyOption option = (StrategyOption) strategyBox.getSelectedItem();
            RouteStrategy strategy = option == null
                    ? selector.select(request)
                    : option.resolve(request, selector);
            RouteDecision decision = new RoutePlanner(strategy).plan(routes, request);

            resultArea.setText(renderDecision(decision, request));
            selectRoute(decision.selectedRoute().id());
            setStatus("Route selected with " + decision.strategyName() + " strategy", false);
        } catch (NoSuitableRouteException exception) {
            resultArea.setText(renderFailure(exception));
            routeTable.clearSelection();
            setStatus("No route satisfies all constraints", true);
        } catch (IllegalArgumentException exception) {
            showInputError(exception.getMessage());
        }
    }

    private void compareAll() {
        try {
            DeliveryRequest request = readRequest();
            StrategyComparisonService comparison = new StrategyComparisonService(List.of(
                    new FastestRouteStrategy(),
                    new CheapestRouteStrategy(),
                    new EcoRouteStrategy(),
                    new BalancedRouteStrategy(),
                    new SafestRouteStrategy()
            ));
            Map<String, RouteDecision> decisions = comparison.compare(routes, request);

            StringBuilder output = new StringBuilder("STRATEGY COMPARISON\n");
            output.append("Request: ").append(request.requestId()).append("\n");
            output.append("All strategies use the same routes and constraints.\n\n");
            decisions.values().forEach(decision -> {
                output.append(String.format(
                        Locale.US,
                        "%-10s -> %-16s score: %.3f%n",
                        decision.strategyName(),
                        decision.selectedRoute().id(),
                        decision.score()
                ));
                output.append("  ").append(decision.explanation()).append("\n");
            });

            resultArea.setText(output.toString());
            routeTable.clearSelection();
            setStatus("Compared " + decisions.size() + " strategies", false);
        } catch (NoSuitableRouteException exception) {
            resultArea.setText(renderFailure(exception));
            routeTable.clearSelection();
            setStatus("No route satisfies all constraints", true);
        } catch (IllegalArgumentException exception) {
            showInputError(exception.getMessage());
        }
    }

    private DeliveryRequest readRequest() {
        String requestId = requestIdField.getText().trim();
        if (requestId.isBlank()) {
            throw new IllegalArgumentException("Request ID must not be empty.");
        }

        DeliveryPriority priority = (DeliveryPriority) priorityBox.getSelectedItem();
        if (priority == null) {
            throw new IllegalArgumentException("Select a delivery priority.");
        }

        double budget = parseNonNegativeDouble(budgetField, "Maximum budget");
        int deadline = parsePositiveInteger(deadlineField, "Deadline");
        double emissionLimit = parseNonNegativeDouble(emissionField, "Emission limit");

        return new DeliveryRequest(
                requestId,
                priority,
                budget,
                deadline,
                ecoPreferredBox.isSelected(),
                emissionLimit
        );
    }

    private double parseNonNegativeDouble(JTextField field, String label) {
        try {
            double value = Double.parseDouble(field.getText().trim().replace(',', '.'));
            if (!Double.isFinite(value) || value < 0.0) {
                throw new NumberFormatException();
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(label + " must be a non-negative number.");
        }
    }

    private int parsePositiveInteger(JTextField field, String label) {
        try {
            int value = Integer.parseInt(field.getText().trim());
            if (value <= 0) {
                throw new NumberFormatException();
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(label + " must be a positive integer.");
        }
    }

    private String renderDecision(RouteDecision decision, DeliveryRequest request) {
        Route route = decision.selectedRoute();
        StringBuilder output = new StringBuilder();
        output.append("SELECTED ROUTE\n");
        output.append("Strategy: ").append(decision.strategyName()).append("\n");
        output.append("Route:    ").append(route.id()).append("\n");
        output.append(String.format(Locale.US, "Score:    %.3f%n", decision.score()));
        output.append(String.format(
                Locale.US,
                "Time:     %d min%nCost:     %.2f%nDistance: %.1f km%nEmissions: %.2f kg%n",
                RouteMetrics.adjustedMinutes(route),
                RouteMetrics.totalCost(route, request),
                route.distanceKm(),
                route.carbonEmissionKg()
        ));
        output.append("\nWHY THIS ROUTE\n").append(decision.explanation()).append("\n");

        if (!decision.rejectedRoutes().isEmpty()) {
            output.append("\nREJECTED BEFORE SCORING\n");
            decision.rejectedRoutes().forEach((routeId, reason) ->
                    output.append("- ").append(routeId).append(": ").append(reason).append("\n")
            );
        }
        return output.toString();
    }

    private String renderFailure(NoSuitableRouteException exception) {
        StringBuilder output = new StringBuilder("NO SUITABLE ROUTE\n");
        output.append(exception.getMessage()).append("\n\nREJECTION REASONS\n");
        exception.rejectedRoutes().forEach((routeId, reason) ->
                output.append("- ").append(routeId).append(": ").append(reason).append("\n")
        );
        return output.toString();
    }

    private void selectRoute(String routeId) {
        for (int modelRow = 0; modelRow < routeModel.getRowCount(); modelRow++) {
            if (routeId.equals(routeModel.getValueAt(modelRow, 0))) {
                int viewRow = routeTable.convertRowIndexToView(modelRow);
                routeTable.setRowSelectionInterval(viewRow, viewRow);
                routeTable.scrollRectToVisible(routeTable.getCellRect(viewRow, 0, true));
                return;
            }
        }
    }

    private void showInputError(String message) {
        setStatus("Invalid request parameters", true);
        JOptionPane.showMessageDialog(
                this,
                message,
                "Invalid input",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void setStatus(String text, boolean error) {
        statusLabel.setText(text);
        statusLabel.setForeground(error ? ERROR : SUCCESS);
    }

    private String format(double value) {
        return String.format(Locale.US, "%.1f", value);
    }

    private static DefaultTableModel createRouteModel() {
        return new DefaultTableModel(
                new Object[]{
                        "Route", "Km", "Base min", "Traffic", "Weather",
                        "CO2 kg", "Safety", "Available"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private enum StrategyOption {
        AUTOMATIC("Automatic selection", null),
        FASTEST("Fastest", FastestRouteStrategy::new),
        CHEAPEST("Cheapest", CheapestRouteStrategy::new),
        ECO("Eco", EcoRouteStrategy::new),
        BALANCED("Balanced", BalancedRouteStrategy::new),
        SAFEST("Safest", SafestRouteStrategy::new);

        private final String label;
        private final Supplier<RouteStrategy> factory;

        StrategyOption(String label, Supplier<RouteStrategy> factory) {
            this.label = label;
            this.factory = factory;
        }

        RouteStrategy resolve(DeliveryRequest request, StrategySelector selector) {
            return factory == null ? selector.select(request) : factory.get();
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
