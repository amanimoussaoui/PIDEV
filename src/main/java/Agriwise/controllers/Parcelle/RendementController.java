package Agriwise.controllers.Parcelle;

import Agriwise.entities.*;
import Agriwise.services.*;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class RendementController implements Initializable {

    @FXML private TabPane tabPane;
    @FXML private Label totalParcellesLabel;
    @FXML private Label totalCulturesLabel;
    @FXML private Label totalRecoltesLabel;
    @FXML private Label totalActivitesLabel;

    // Services
    private final ParcelleService parcelleService = new ParcelleService();
    private final CultureService cultureService = new CultureService();
    private final RecolteService recolteService = new RecolteService();
    private final ActiviteService activiteService = new ActiviteService();

    // Chart colors based on the theme defined in CSS
    private final String[] chartColors = {
            "#1F4E3D", "#2A9D8F", "#E9C46A", "#F4A261", "#E76F51"
    };

    // Date formatter for month names
    private final SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.FRENCH);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load summary data
        loadSummaryData();

        // Setup tabs with charts
        setupParcellesTab();
        setupCulturesTab();
        setupRecoltesTab();
        setupActivitesTab();

        // Add animation to tabs when switching
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            VBox content = (VBox) ((ScrollPane) newVal.getContent()).getContent();
            animateContent(content);
        });

        // Initial animation for first tab
        Platform.runLater(() -> {
            VBox content = (VBox) ((ScrollPane) tabPane.getSelectionModel().getSelectedItem().getContent()).getContent();
            animateContent(content);
        });
    }

    private void animateContent(VBox content) {
        // Add fade-in animation to content
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(400), content);
        fadeTransition.setFromValue(0.3);
        fadeTransition.setToValue(1.0);
        fadeTransition.play();
    }

    // In RendementController.java
    private void loadSummaryData() {
        try {
            UserSession userSession = UserSession.getInstance();
            int userId = userSession.getUserId();

            System.out.println("Loading summary data for user ID: " + userId);

            // Load counts with default values if empty
            List<Parcelle> parcelles = parcelleService.getParcellesByUserId(userId);
            List<Culture> cultures = cultureService.getCulturesByUserId(userId);
            List<Recolte> recoltes = recolteService.getRecoltesByUserId(userId); // Use the new method
            List<Activite> activites = activiteService.getActivitesByUserId(userId);

            System.out.println("Found parcelles: " + parcelles.size());
            System.out.println("Found recoltes: " + recoltes.size());

            // Set text values with defensive programming
            Platform.runLater(() -> {
                totalParcellesLabel.setText(String.valueOf(parcelles.size()));
                totalCulturesLabel.setText(String.valueOf(cultures.size()));
                totalRecoltesLabel.setText(String.valueOf(recoltes.size()));
                totalActivitesLabel.setText(String.valueOf(activites.size()));
            });
        } catch (Exception e) {
            e.printStackTrace();
            // Set default values if there's an error
            Platform.runLater(() -> {
                totalParcellesLabel.setText("0");
                totalCulturesLabel.setText("0");
                totalRecoltesLabel.setText("0");
                totalActivitesLabel.setText("0");
            });
        }
    }

    private void setupParcellesTab() {
        VBox parcelleTabContent = (VBox) ((ScrollPane) tabPane.getTabs().get(0).getContent()).getContent();
        parcelleTabContent.getChildren().clear();

        // First row - two charts side by side
        HBox firstRow = createResponsiveRow();

        // Chart 1: Soil Type Distribution
        PieChart soilTypeChart = createPieChart(
                "Répartition des Parcelles par Type de Sol",
                getSoilTypeDistribution());
        VBox soilTypeContainer = createChartContainer(soilTypeChart, "soil-type-chart");

        // Chart 2: Parcel Yield Over Time
        LineChart<String, Number> yieldChart = createLineChart(
                "Rendement des Parcelles par Mois",
                "Mois",
                "Rendement (kg/ha)",
                getParcelleYieldData());
        VBox yieldChartContainer = createChartContainer(yieldChart, "yield-chart");

        firstRow.getChildren().addAll(soilTypeContainer, yieldChartContainer);
        parcelleTabContent.getChildren().add(firstRow);
    }

    private void setupCulturesTab() {
        VBox cultureTabContent = (VBox) ((ScrollPane) tabPane.getTabs().get(1).getContent()).getContent();
        cultureTabContent.getChildren().clear();

        // First row - two charts side by side
        HBox firstRow = createResponsiveRow();

        // Chart 1: Crop Type Distribution
        PieChart cropTypeChart = createPieChart(
                "Répartition des Cultures par Type",
                getCropTypeDistribution());
        VBox cropTypeContainer = createChartContainer(cropTypeChart, "crop-type-chart");

        // Chart 2: Crop Status Distribution
        PieChart cropStatusChart = createPieChart(
                "Statut de Croissance des Cultures",
                getCropStatusDistribution());
        VBox cropStatusContainer = createChartContainer(cropStatusChart, "crop-status-chart");

        firstRow.getChildren().addAll(cropTypeContainer, cropStatusContainer);

        // Second row - Add two more charts when scrolling
        HBox secondRow = createResponsiveRow();

        // Chart 3: Culture Yield by Type
        BarChart<String, Number> cultureYieldChart = createBarChart(
                "Rendement des Cultures par Type",
                "Type de Culture",
                "Rendement (kg/ha)",
                getCultureYieldData());
        VBox cultureYieldContainer = createChartContainer(cultureYieldChart, "culture-yield-chart");

        // Chart 4: Récolte by Culture Type
        PieChart recolteCultureTypeChart = createPieChart(
                "Répartition des Récoltes par Type de Culture",
                getRecolteCultureTypeDistribution());
        VBox recolteCultureTypeContainer = createChartContainer(recolteCultureTypeChart, "recolte-culture-chart");

        secondRow.getChildren().addAll(cultureYieldContainer, recolteCultureTypeContainer);

        cultureTabContent.getChildren().addAll(firstRow, secondRow);
    }

    private void setupRecoltesTab() {
        VBox recolteTabContent = (VBox) ((ScrollPane) tabPane.getTabs().get(2).getContent()).getContent();
        recolteTabContent.getChildren().clear();

        // First row - two charts side by side
        HBox firstRow = createResponsiveRow();

        // Chart 1: Harvest Quantity Over Time
        LineChart<String, Number> quantityChart = createLineChart(
                "Quantité des Récoltes par Mois",
                "Mois",
                "Quantité (kg)",
                getRecolteQuantityData());
        VBox quantityChartContainer = createChartContainer(quantityChart, "quantity-chart");

        // Chart 2: Revenue Display
        VBox revenueBox = createModernRevenueDisplay();
        VBox revenueContainer = createChartContainer(revenueBox, "revenue-chart");

        firstRow.getChildren().addAll(quantityChartContainer, revenueContainer);

        // Second row
        HBox secondRow = createResponsiveRow();

        // Chart 3: Harvest Quality Distribution
        PieChart qualityChart = createPieChart(
                "Répartition des Récoltes par Qualité",
                getRecolteQualityDistribution());
        VBox qualityChartContainer = createChartContainer(qualityChart, "quality-chart");

        // Chart 4: Monthly Revenue Trends (new chart)
        BarChart<String, Number> revenueChart = createBarChart(
                "Tendances des Revenus Mensuels",
                "Mois",
                "Revenu (TND)",
                getMonthlyRevenueData());
        VBox revenueChartContainer = createChartContainer(revenueChart, "monthly-revenue-chart");

        secondRow.getChildren().addAll(qualityChartContainer, revenueChartContainer);

        recolteTabContent.getChildren().addAll(firstRow, secondRow);
    }

    private void setupActivitesTab() {
        VBox activiteTabContent = (VBox) ((ScrollPane) tabPane.getTabs().get(3).getContent()).getContent();
        activiteTabContent.getChildren().clear();

        // First row - two charts side by side
        HBox firstRow = createResponsiveRow();

        // Chart 1: Activity Type Frequency
        BarChart<String, Number> activityTypeChart = createBarChart(
                "Fréquence des Activités par Type",
                "Type",
                "Nombre",
                getActivityTypeFrequencyData());
        VBox activityTypeContainer = createChartContainer(activityTypeChart, "activity-type-chart");

        // Chart 2: Activity Distribution by Crop
        PieChart activityCropChart = createPieChart(
                "Répartition des Activités par Culture",
                getActivityCropDistribution());
        VBox activityCropContainer = createChartContainer(activityCropChart, "activity-crop-chart");

        firstRow.getChildren().addAll(activityTypeContainer, activityCropContainer);

        // Second row
        HBox secondRow = createResponsiveRow();

        // Chart 3: Activity Frequency Over Time
        LineChart<String, Number> activityTimeChart = createLineChart(
                "Fréquence des Activités par Mois",
                "Mois",
                "Nombre d'activités",
                getActivityFrequencyOverTimeData());
        VBox activityTimeContainer = createChartContainer(activityTimeChart, "activity-time-chart");

        // Chart 4: Activity Status Distribution (new chart)
        PieChart activityStatusChart = createPieChart(
                "Statut des Activités",
                getActivityStatusDistribution());
        VBox activityStatusContainer = createChartContainer(activityStatusChart, "activity-status-chart");

        secondRow.getChildren().addAll(activityTimeContainer, activityStatusContainer);

        activiteTabContent.getChildren().addAll(firstRow, secondRow);
    }

    // RESPONSIVE LAYOUT HELPERS
    private HBox createResponsiveRow() {
        HBox row = new HBox();
        row.setSpacing(20);
        row.setPadding(new Insets(10));
        row.setAlignment(Pos.CENTER);
        row.getStyleClass().add("chart-row");
        HBox.setHgrow(row, Priority.ALWAYS);
        return row;
    }

    private VBox createChartContainer(javafx.scene.Node chart, String styleClass) {
        VBox container = new VBox();
        container.getStyleClass().addAll("modern-chart-container", styleClass);
        container.getChildren().add(chart);
        VBox.setVgrow(chart, Priority.ALWAYS);
        HBox.setHgrow(container, Priority.ALWAYS);
        return container;
    }

    private VBox createModernRevenueDisplay() {
        VBox revenueContainer = new VBox();
        revenueContainer.setAlignment(Pos.CENTER);
        revenueContainer.getStyleClass().add("revenue-display");
        revenueContainer.setSpacing(20);

        Label title = new Label("Revenu Total des Récoltes");
        title.getStyleClass().add("revenue-title");

        UserSession userSession = UserSession.getInstance();
        int userId = userSession.getUserId();

        // Use the new method to get recoltes by user ID
        List<Recolte> userRecoltes = recolteService.getRecoltesByUserId(userId);

        double totalRevenue = userRecoltes.stream()
                .mapToDouble(r -> r.getQuantite() * r.getPrixUnitaire())
                .sum();

        Label revenueValue = new Label(String.format("%.2f TND", totalRevenue));
        revenueValue.getStyleClass().add("revenue-value");

        // Calculate trend
        double trendPercentage = calculateRevenueTrend(userId);
        String trendText = String.format("%.1f%% vs. mois précédent", trendPercentage);

        HBox trendBox = new HBox();
        trendBox.setAlignment(Pos.CENTER);
        trendBox.setSpacing(10);

        Label trendLabel = new Label(trendText);
        trendLabel.setStyle(trendPercentage >= 0 ?
                "-fx-text-fill: #2A9D8F; -fx-font-weight: bold;" :
                "-fx-text-fill: #E76F51; -fx-font-weight: bold;");

        trendBox.getChildren().add(trendLabel);
        revenueContainer.getChildren().addAll(title, revenueValue, trendBox);

        return revenueContainer;
    }


    private double calculateRevenueTrend(int userId) {
        // Get current month and previous month revenue for comparison
        Calendar cal = Calendar.getInstance();
        int currentMonth = cal.get(Calendar.MONTH);
        int currentYear = cal.get(Calendar.YEAR);

        cal.add(Calendar.MONTH, -1);
        int prevMonth = cal.get(Calendar.MONTH);
        int prevYear = cal.get(Calendar.YEAR);

        double currentRevenue = getRevenueForMonth(userId, currentMonth, currentYear);
        double prevRevenue = getRevenueForMonth(userId, prevMonth, prevYear);

        if (prevRevenue == 0) return 0;
        return ((currentRevenue - prevRevenue) / prevRevenue) * 100;
    }

    private double getRevenueForMonth(int userId, int month, int year) {
        List<Recolte> userRecoltes = recolteService.getRecoltesByUserId(userId);

        return userRecoltes.stream()
                .filter(r -> r.getDateRecolte() != null)
                .filter(r -> {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(r.getDateRecolte());
                    return cal.get(Calendar.MONTH) == month &&
                            cal.get(Calendar.YEAR) == year;
                })
                .mapToDouble(r -> r.getQuantite() * r.getPrixUnitaire())
                .sum();
    }


    // CHART CREATION HELPER METHODS
    private PieChart createPieChart(String title, ObservableList<PieChart.Data> data) {
        PieChart chart = new PieChart(data);
        chart.setTitle(title);
        chart.setLegendVisible(true);
        chart.setLabelsVisible(true);
        chart.setAnimated(true);

        int i = 0;
        for (PieChart.Data d : data) {
            String color = chartColors[i % chartColors.length];
            d.getNode().setStyle("-fx-pie-color: " + color + ";");
            i++;
        }
        return chart;
    }

    private BarChart<String, Number> createBarChart(String title, String xAxisLabel, String yAxisLabel,
                                                    XYChart.Series<String, Number> series) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel(xAxisLabel);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yAxisLabel);

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.setAnimated(true);
        chart.getData().add(series);
        chart.setLegendVisible(false);

        for (int i = 0; i < series.getData().size(); i++) {
            String color = chartColors[i % chartColors.length];
            series.getData().get(i).getNode().setStyle("-fx-bar-fill: " + color + ";");
        }
        return chart;
    }

    private LineChart<String, Number> createLineChart(String title, String xAxisLabel, String yAxisLabel,
                                                      List<XYChart.Series<String, Number>> seriesList) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel(xAxisLabel);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yAxisLabel);

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.setAnimated(true);
        chart.setCreateSymbols(true);
        chart.getData().addAll(seriesList);

        for (int i = 0; i < seriesList.size(); i++) {
            String color = chartColors[i % chartColors.length];
            seriesList.get(i).getNode().setStyle("-fx-stroke: " + color + ";");
        }
        return chart;
    }

    // DATA RETRIEVAL METHODS (updated to use real data)
    private ObservableList<PieChart.Data> getSoilTypeDistribution() {
        UserSession userSession = UserSession.getInstance();
        int userId = userSession.getUserId();

        Map<String, Long> soilTypeCounts = parcelleService.getParcellesByUserId(userId).stream()
                .collect(Collectors.groupingBy(Parcelle::getTypeSol, Collectors.counting()));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        soilTypeCounts.forEach((type, count) -> pieChartData.add(new PieChart.Data(type, count)));

        return pieChartData;
    }

    private List<XYChart.Series<String, Number>> getParcelleYieldData() {
        UserSession userSession = UserSession.getInstance();
        int userId = userSession.getUserId();

        List<Parcelle> parcelles = parcelleService.getParcellesByUserId(userId);
        List<XYChart.Series<String, Number>> seriesList = new ArrayList<>();

        // Group recoltes by parcelle and month
        Map<Integer, Map<Integer, Double>> parcelleMonthlyYield = new HashMap<>();

        // Initialize structure
        for (Parcelle p : parcelles) {
            parcelleMonthlyYield.put(p.getId(), new HashMap<>());
            for (int month = 0; month < 12; month++) {
                parcelleMonthlyYield.get(p.getId()).put(month, 0.0);
            }
        }

        // Populate with actual data
        List<Recolte> recoltes = recolteService.getRecoltesByUserId(userId); // Instead of filtering

        for (Recolte r : recoltes) {
            if (r.getDateRecolte() != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(r.getDateRecolte());
                int month = cal.get(Calendar.MONTH);
                int parcelleId = r.getCulture().getParcelle().getId();

                // Calculate yield in kg/ha
                float superficie = r.getCulture().getParcelle().getSuperficie();
                double yield = superficie > 0 ? r.getQuantite() / superficie : 0;

                parcelleMonthlyYield.get(parcelleId).merge(month, yield, Double::sum);
            }
        }

        // Create series for top 3 parcelles by total yield
        List<Parcelle> topParcelles = parcelles.stream()
                .sorted((p1, p2) -> {
                    double y1 = parcelleMonthlyYield.get(p1.getId()).values().stream().mapToDouble(Double::doubleValue).sum();
                    double y2 = parcelleMonthlyYield.get(p2.getId()).values().stream().mapToDouble(Double::doubleValue).sum();
                    return Double.compare(y2, y1);
                })
                .limit(3)
                .collect(Collectors.toList());

        for (Parcelle p : topParcelles) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(p.getNom());

            for (int month = 0; month < 12; month++) {
                Calendar cal = Calendar.getInstance(); // Add this line
                cal.set(Calendar.MONTH, month);
                String monthName = monthFormat.format(cal.getTime());
                double yield = parcelleMonthlyYield.get(p.getId()).get(month);
                series.getData().add(new XYChart.Data<>(monthName, yield));
            }

            seriesList.add(series);
        }

        return seriesList;
    }

    private ObservableList<PieChart.Data> getCropTypeDistribution() {
        UserSession userSession = UserSession.getInstance();
        int userId = userSession.getUserId();

        Map<String, Long> cropTypeCounts = cultureService.getCulturesByUserId(userId).stream()
                .collect(Collectors.groupingBy(Culture::getNomCulture, Collectors.counting()));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        cropTypeCounts.forEach((type, count) -> pieChartData.add(new PieChart.Data(type, count)));

        return pieChartData;
    }

    private ObservableList<PieChart.Data> getCropStatusDistribution() {
        UserSession userSession = UserSession.getInstance();
        int userId = userSession.getUserId();

        Map<String, Long> statusCounts = cultureService.getCulturesByUserId(userId).stream()
                .collect(Collectors.groupingBy(Culture::getStatut, Collectors.counting()));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        statusCounts.forEach((status, count) -> pieChartData.add(new PieChart.Data(status, count)));

        return pieChartData;
    }

    private XYChart.Series<String, Number> getCultureYieldData() {
        UserSession userSession = UserSession.getInstance();
        int userId = userSession.getUserId();

        // Group cultures by type and calculate average yield
        Map<String, Double> yieldByType = cultureService.getCulturesByUserId(userId).stream()
                .collect(Collectors.groupingBy(
                        Culture::getNomCulture,
                        Collectors.averagingDouble(c -> {
                            Recolte r = recolteService.getRecoltesByCultureId(c.getId());
                            if (r != null && c.getParcelle() != null && c.getParcelle().getSuperficie() > 0) {
                                return r.getQuantite() / c.getParcelle().getSuperficie();
                            }
                            return 0;
                        })
                ));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Rendement par type de culture");

        yieldByType.forEach((type, yield) ->
                series.getData().add(new XYChart.Data<>(type, yield)));

        return series;
    }


    private ObservableList<PieChart.Data> getRecolteCultureTypeDistribution() {
        UserSession userSession = UserSession.getInstance();
        int userId = userSession.getUserId();

        // Use the new method to get only this user's recoltes
        List<Recolte> userRecoltes = recolteService.getRecoltesByUserId(userId);

        Map<String, Long> cultureTypeCounts = userRecoltes.stream()
                .filter(r -> r.getCulture() != null) // Additional null check
                .collect(Collectors.groupingBy(
                        r -> r.getCulture().getNomCulture(),
                        Collectors.counting()
                ));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        cultureTypeCounts.forEach((type, count) ->
                pieChartData.add(new PieChart.Data(type, count))
        );

        return pieChartData;
    }


    private List<XYChart.Series<String, Number>> getRecolteQuantityData() {
        UserSession userSession = UserSession.getInstance();
        int userId = userSession.getUserId();

        // Group recoltes by month
        Map<Integer, Double> monthlyQuantity = new HashMap<>();
        for (int month = 0; month < 12; month++) {
            monthlyQuantity.put(month, 0.0);
        }

        // Use the new method to get recoltes by user ID
        List<Recolte> recoltes = recolteService.getRecoltesByUserId(userId);

        for (Recolte r : recoltes) {
            if (r.getDateRecolte() != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(r.getDateRecolte());
                int month = cal.get(Calendar.MONTH);
                monthlyQuantity.merge(month, (double)r.getQuantite(), Double::sum);
            }
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Quantité récoltée");

        Calendar cal = Calendar.getInstance();
        for (int month = 0; month < 12; month++) {
            cal.set(Calendar.MONTH, month);
            String monthName = monthFormat.format(cal.getTime());
            series.getData().add(new XYChart.Data<>(monthName, monthlyQuantity.get(month)));
        }

        return List.of(series);
    }


    private ObservableList<PieChart.Data> getRecolteQualityDistribution() {
        UserSession userSession = UserSession.getInstance();
        int userId = userSession.getUserId();

        // Use the new method to get only this user's recoltes
        List<Recolte> userRecoltes = recolteService.getRecoltesByUserId(userId);

        Map<String, Long> qualityCounts = userRecoltes.stream()
                .collect(Collectors.groupingBy(
                        Recolte::getQualite,
                        Collectors.counting()
                ));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        qualityCounts.forEach((quality, count) ->
                pieChartData.add(new PieChart.Data(quality, count))
        );

        return pieChartData;
    }


    private XYChart.Series<String, Number> getActivityTypeFrequencyData() {
        UserSession userSession = UserSession.getInstance();
        int userId = userSession.getUserId();

        Map<String, Long> activityTypeCounts = activiteService.getActivitesByUserId(userId).stream()
                .collect(Collectors.groupingBy(Activite::getType, Collectors.counting()));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nombre d'activités");

        activityTypeCounts.forEach((type, count) ->
                series.getData().add(new XYChart.Data<>(type, count)));

        return series;
    }

    private ObservableList<PieChart.Data> getActivityCropDistribution() {
        UserSession userSession = UserSession.getInstance();
        int userId = userSession.getUserId();

        Map<String, Long> activityCultureCounts = activiteService.getActivitesByUserId(userId).stream()
                .collect(Collectors.groupingBy(a -> a.getCulture().getNomCulture(), Collectors.counting()));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        activityCultureCounts.forEach((culture, count) -> pieChartData.add(new PieChart.Data(culture, count)));

        return pieChartData;
    }

    private List<XYChart.Series<String, Number>> getActivityFrequencyOverTimeData() {
        UserSession userSession = UserSession.getInstance();
        int userId = userSession.getUserId();

        // Group activities by month
        Map<Integer, Long> monthlyActivityCount = new HashMap<>();
        for (int month = 0; month < 12; month++) {
            monthlyActivityCount.put(month, 0L);
        }

        activiteService.getActivitesByUserId(userId).stream()
                .forEach(a -> {
                    if (a.getDate() != null) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(a.getDate());
                        int month = cal.get(Calendar.MONTH);
                        monthlyActivityCount.merge(month, 1L, Long::sum);
                    }
                });

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Activités");

        Calendar cal = Calendar.getInstance();
        for (int month = 0; month < 12; month++) {
            cal.set(Calendar.MONTH, month);
            String monthName = monthFormat.format(cal.getTime());
            series.getData().add(new XYChart.Data<>(monthName, monthlyActivityCount.get(month)));
        }

        return List.of(series);
    }

    private XYChart.Series<String, Number> getMonthlyRevenueData() {
        UserSession userSession = UserSession.getInstance();
        int userId = userSession.getUserId();

        // Group revenue by month
        Map<Integer, Double> monthlyRevenue = new HashMap<>();
        for (int month = 0; month < 12; month++) {
            monthlyRevenue.put(month, 0.0);
        }

        // Use the new method to get recoltes by user ID
        List<Recolte> recoltes = recolteService.getRecoltesByUserId(userId);

        for (Recolte r : recoltes) {
            if (r.getDateRecolte() != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(r.getDateRecolte());
                int month = cal.get(Calendar.MONTH);
                double revenue = r.getQuantite() * r.getPrixUnitaire();
                monthlyRevenue.merge(month, revenue, Double::sum);
            }
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenu mensuel");

        Calendar cal = Calendar.getInstance();
        for (int month = 0; month < 12; month++) {
            cal.set(Calendar.MONTH, month);
            String monthName = monthFormat.format(cal.getTime());
            series.getData().add(new XYChart.Data<>(monthName, monthlyRevenue.get(month)));
        }

        return series;
    }

    private ObservableList<PieChart.Data> getActivityStatusDistribution() {
        UserSession userSession = UserSession.getInstance();
        int userId = userSession.getUserId();

        // Get user's activities
        List<Activite> userActivities = activiteService.getActivitesByUserId(userId);

        // Initialize counters
        long completed = 0;
        long inProgress = 0;
        long planned = 0;
        long postponed = 0;

        Date currentDate = new Date();

        for (Activite activity : userActivities) {
            if (activity.getDate() == null) {
                postponed++; // Activities without date are considered postponed
                continue;
            }

            // Compare activity date with current date
            int comparison = activity.getDate().compareTo(currentDate);

            if (comparison < 0) {
                completed++; // Past date = completed
            } else if (comparison == 0) {
                inProgress++; // Today = in progress
            } else {
                planned++; // Future date = planned
            }
        }

        // Create pie chart data
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Terminé", completed),
                new PieChart.Data("En cours", inProgress),
                new PieChart.Data("Planifié", planned),
                new PieChart.Data("Sans date", postponed)
        );

        return pieChartData;
    }
}