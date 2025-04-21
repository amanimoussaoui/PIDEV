
package Agriwise.controllers;

import Agriwise.Main;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class SidebarBackendController {

    // Main sidebar components
    @FXML
    private VBox sidebarExpanded;
    @FXML
    private VBox sidebarCollapsed;

    // Submenu components and arrows
    @FXML
    private VBox gestionAgricoleSubmenu;
    @FXML
    private FontAwesomeIconView gestionAgricoleArrow;

    @FXML
    private VBox marketplaceSubmenu;
    @FXML
    private FontAwesomeIconView marketplaceArrow;

    @FXML
    private VBox materielsSubmenu;
    @FXML
    private FontAwesomeIconView materielsArrow;

    @FXML
    private VBox formationsSubmenu;
    @FXML
    private FontAwesomeIconView formationsArrow;

    @FXML
    private VBox candidatureSubmenu;
    @FXML
    private FontAwesomeIconView candidatureArrow;

    // Main buttons
    @FXML private Button dashboardBtn, dashboardIconBtn;
    @FXML private Button usersBtn, usersIconBtn;
    @FXML private Button gestionAgricoleBtn, gestionAgricoleIconBtn;
    @FXML private Button marketplaceBtn, marketplaceIconBtn;
    @FXML private Button materielsBtn, materielsIconBtn;
    @FXML private Button formationsBtn, formationsIconBtn;
    @FXML private Button candidatureBtn, candidatureIconBtn;
    @FXML private Button logoutBtn, logoutIconBtn;
    @FXML private Button toggleBtn, expandBtn;

    // Submenu buttons
    // Agricultural submenu
    @FXML private Button parcelleBtn;
    @FXML private Button cultureBtn;
    @FXML private Button recolteBtn;
    @FXML private Button activiteBtn;

    // Marketplace submenu
    @FXML private Button produitsBtn;
    @FXML private Button commandesBtn;

    // Materials submenu
    @FXML private Button listeMaterielsBtn;
    @FXML private Button reservationsBtn;

    // Formations submenu
    @FXML private Button listeFormationsBtn;
    @FXML private Button participationsBtn;

    // Candidature submenu
    @FXML private Button terrainsBtn;
    @FXML private Button listeCandidatureBtn;

    // State tracking
    private boolean isExpanded = true;
    private boolean isGestionAgricoleSubmenuOpen = false;
    private boolean isMarketplaceSubmenuOpen = false;
    private boolean isMaterielsSubmenuOpen = false;
    private boolean isFormationsSubmenuOpen = false;
    private boolean isCandidatureSubmenuOpen = false;

    private Button currentActiveButton = null;
    private String currentActiveCategory = null;

    // Maps for navigation
    private Map<Button, String> buttonPathMap = new HashMap<>();
    private Map<Button, Button> buttonPairs = new HashMap<>();
    private Map<Button, String> buttonCategoryMap = new HashMap<>();
    private Map<Button, Button> submenuParentMap = new HashMap<>();
    private List<VBox> allSubmenus = new ArrayList<>();

    @FXML
    private void initialize() {
        setupNavigationPaths();
        setupCategoryMaps();
        populateSubmenus();

        // Set default view to Dashboard
        setActiveButton(dashboardBtn);
    }

    private void setupNavigationPaths() {
        // Main sections
        buttonPathMap.put(dashboardBtn, "/Agriwise/views/DashboardView.fxml");
        buttonPathMap.put(dashboardIconBtn, "/Agriwise/views/DashboardView.fxml");

        buttonPathMap.put(usersBtn, "/Agriwise/views/Utilisateur");
        buttonPathMap.put(usersIconBtn, "/Agriwise/views/Utilisateur");

        // Configure paths for marketplace section
        buttonPathMap.put(marketplaceBtn, "/Agriwise/views/Produit");
        buttonPathMap.put(marketplaceIconBtn, "/Agriwise/views/Produit");
        buttonPathMap.put(produitsBtn, "/Agriwise/views/Produit");
        buttonPathMap.put(commandesBtn, "/Agriwise/views/Produit");

        // Configure paths for agricultural section
        buttonPathMap.put(gestionAgricoleBtn, "/Agriwise/views/Parcelle/BackendParcelleView.fxml");
        buttonPathMap.put(gestionAgricoleIconBtn, "/Agriwise/views/Parcelle/BackendParcelleView.fxml");
        buttonPathMap.put(parcelleBtn, "/Agriwise/views/Parcelle/BackendParcelleView.fxml");
        buttonPathMap.put(cultureBtn, "/Agriwise/views/Culture/BackendCultureView.fxml");
        buttonPathMap.put(recolteBtn, "/Agriwise/views/Recolte/BackendRecolteView.fxml");
        buttonPathMap.put(activiteBtn, "/Agriwise/views/Activite/BackendActiviteView.fxml");

        // Configure paths for materials section
        buttonPathMap.put(materielsBtn, "/Agriwise/views/Materiel");
        buttonPathMap.put(materielsIconBtn, "/Agriwise/views/Materiel");
        buttonPathMap.put(listeMaterielsBtn, "/Agriwise/views/Materiel");
        buttonPathMap.put(reservationsBtn, "/Agriwise/views/Materiel");

        // Configure paths for formations section
        buttonPathMap.put(formationsBtn, "/Agriwise/views/Formation");
        buttonPathMap.put(formationsIconBtn, "/Agriwise/views/Formation");
        buttonPathMap.put(listeFormationsBtn, "/Agriwise/views/Formation");
        buttonPathMap.put(participationsBtn, "/Agriwise/views/Formation");

        // Configure paths for candidature section
        buttonPathMap.put(candidatureBtn, "/Agriwise/views/Candidature");
        buttonPathMap.put(candidatureIconBtn, "/Agriwise/views/Candidature");
        buttonPathMap.put(terrainsBtn, "/Agriwise/views/Candidature");
        buttonPathMap.put(listeCandidatureBtn, "/Agriwise/views/Candidature");

        // Configure paths for Logout
        buttonPathMap.put(logoutBtn, "/Agriwise/views/Utilisateur");
        buttonPathMap.put(logoutIconBtn, "/Agriwise/views/Utilisateur");
    }

    private void setupCategoryMaps() {
        // Main menu items
        buttonCategoryMap.put(dashboardBtn, "dashboard");
        buttonCategoryMap.put(dashboardIconBtn, "dashboard");

        buttonCategoryMap.put(usersBtn, "system");
        buttonCategoryMap.put(usersIconBtn, "system");

        // Marketplace section
        buttonCategoryMap.put(marketplaceBtn, "marketplace");
        buttonCategoryMap.put(marketplaceIconBtn, "marketplace");
        buttonCategoryMap.put(produitsBtn, "marketplace");
        buttonCategoryMap.put(commandesBtn, "marketplace");

        submenuParentMap.put(produitsBtn, marketplaceBtn);
        submenuParentMap.put(commandesBtn, marketplaceBtn);

        // Agricultural Management section
        buttonCategoryMap.put(gestionAgricoleBtn, "agricultural");
        buttonCategoryMap.put(gestionAgricoleIconBtn, "agricultural");
        buttonCategoryMap.put(parcelleBtn, "agricultural");
        buttonCategoryMap.put(cultureBtn, "agricultural");
        buttonCategoryMap.put(recolteBtn, "agricultural");
        buttonCategoryMap.put(activiteBtn, "agricultural");

        submenuParentMap.put(parcelleBtn, gestionAgricoleBtn);
        submenuParentMap.put(cultureBtn, gestionAgricoleBtn);
        submenuParentMap.put(recolteBtn, gestionAgricoleBtn);
        submenuParentMap.put(activiteBtn, gestionAgricoleBtn);

        // Equipment section
        buttonCategoryMap.put(materielsBtn, "equipment");
        buttonCategoryMap.put(materielsIconBtn, "equipment");
        buttonCategoryMap.put(listeMaterielsBtn, "equipment");
        buttonCategoryMap.put(reservationsBtn, "equipment");

        submenuParentMap.put(listeMaterielsBtn, materielsBtn);
        submenuParentMap.put(reservationsBtn, materielsBtn);

        // Training section
        buttonCategoryMap.put(formationsBtn, "training");
        buttonCategoryMap.put(formationsIconBtn, "training");
        buttonCategoryMap.put(listeFormationsBtn, "training");
        buttonCategoryMap.put(participationsBtn, "training");

        submenuParentMap.put(listeFormationsBtn, formationsBtn);
        submenuParentMap.put(participationsBtn, formationsBtn);

        // Application section
        buttonCategoryMap.put(candidatureBtn, "applications");
        buttonCategoryMap.put(candidatureIconBtn, "applications");
        buttonCategoryMap.put(terrainsBtn, "applications");
        buttonCategoryMap.put(listeCandidatureBtn, "applications");

        submenuParentMap.put(terrainsBtn, candidatureBtn);
        submenuParentMap.put(listeCandidatureBtn, candidatureBtn);

        // Logout buttons don't need categories as they lead to login screen
        buttonCategoryMap.put(logoutBtn, "logout");
        buttonCategoryMap.put(logoutIconBtn, "logout");
    }


    private void toggleSubmenuVisibility(VBox submenu, FontAwesomeIconView arrow) {
        if (!submenu.isVisible()) {
            submenu.setVisible(true);
            submenu.setManaged(true);

            RotateTransition rotateTransition = new RotateTransition(Duration.millis(200), arrow);
            rotateTransition.setFromAngle(0);
            rotateTransition.setToAngle(180);
            rotateTransition.play();

            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), submenu);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        } else {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), submenu);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                submenu.setVisible(false);
                submenu.setManaged(false);
            });
            fadeOut.play();

            RotateTransition rotateTransition = new RotateTransition(Duration.millis(200), arrow);
            rotateTransition.setFromAngle(180);
            rotateTransition.setToAngle(0);
            rotateTransition.play();
        }
    }

    private void openSubmenu(VBox submenu, FontAwesomeIconView arrow) {
        if (!submenu.isVisible()) {
            submenu.setVisible(true);
            submenu.setManaged(true);
            arrow.setRotate(180);
        }
    }


    private void setActiveButton(Button button) {
        // Step 1: Clear active state from all buttons first
        clearAllActiveStates();

        // Step 2: Set new button as active
        button.getStyleClass().add("active");
        Button pairedCurrentButton = buttonPairs.get(button);
        if (pairedCurrentButton != null) {
            pairedCurrentButton.getStyleClass().add("active");
        }

        // Step 3: If it's a submenu item, also set its parent as active
        if (submenuParentMap.containsKey(button)) {
            Button parentButton = submenuParentMap.get(button);
            parentButton.getStyleClass().add("active");

            Button parentPair = buttonPairs.get(parentButton);
            if (parentPair != null) {
                parentPair.getStyleClass().add("active");
            }
        }

        currentActiveButton = button;
        currentActiveCategory = buttonCategoryMap.get(button);
    }


    @FXML
    private void toggleSidebar() {
        if (isExpanded) {
            // Collapse animation
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), sidebarExpanded);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                sidebarExpanded.setVisible(false);
                sidebarExpanded.setManaged(false);
                sidebarCollapsed.setVisible(true);
                sidebarCollapsed.setManaged(true);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), sidebarCollapsed);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeOut.play();
        } else {
            // Expand animation
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), sidebarCollapsed);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                sidebarCollapsed.setVisible(false);
                sidebarCollapsed.setManaged(false);
                sidebarExpanded.setVisible(true);
                sidebarExpanded.setManaged(true);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), sidebarExpanded);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();

                // Ensure we restore active submenu state
                restoreActiveSubmenuState();
            });
            fadeOut.play();
        }

        isExpanded = !isExpanded;
    }


    private void populateSubmenus() {
        allSubmenus.add(gestionAgricoleSubmenu);
        allSubmenus.add(marketplaceSubmenu);
        allSubmenus.add(materielsSubmenu);
        allSubmenus.add(formationsSubmenu);
        allSubmenus.add(candidatureSubmenu);
    }





    @FXML
    private void toggleSubmenu(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();

        if (clickedButton == gestionAgricoleBtn) {
            toggleSubmenuVisibility(gestionAgricoleSubmenu, gestionAgricoleArrow);
            isGestionAgricoleSubmenuOpen = !isGestionAgricoleSubmenuOpen;
        } else if (clickedButton == marketplaceBtn) {
            toggleSubmenuVisibility(marketplaceSubmenu, marketplaceArrow);
            isMarketplaceSubmenuOpen = !isMarketplaceSubmenuOpen;
        } else if (clickedButton == materielsBtn) {
            toggleSubmenuVisibility(materielsSubmenu, materielsArrow);
            isMaterielsSubmenuOpen = !isMaterielsSubmenuOpen;
        } else if (clickedButton == formationsBtn) {
            toggleSubmenuVisibility(formationsSubmenu, formationsArrow);
            isFormationsSubmenuOpen = !isFormationsSubmenuOpen;
        } else if (clickedButton == candidatureBtn) {
            toggleSubmenuVisibility(candidatureSubmenu, candidatureArrow);
            isCandidatureSubmenuOpen = !isCandidatureSubmenuOpen;
        }
    }

    @FXML
    private void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String fxmlPath = buttonPathMap.get(clickedButton);

        if (fxmlPath != null) {
            // Handle submenu parent expansion if needed
            if (submenuParentMap.containsKey(clickedButton)) {
                Button parentButton = submenuParentMap.get(clickedButton);
                String category = buttonCategoryMap.get(clickedButton);

                if (category.equals("agricultural") && !isGestionAgricoleSubmenuOpen) {
                    openSubmenu(gestionAgricoleSubmenu, gestionAgricoleArrow);
                    isGestionAgricoleSubmenuOpen = true;
                } else if (category.equals("marketplace") && !isMarketplaceSubmenuOpen) {
                    openSubmenu(marketplaceSubmenu, marketplaceArrow);
                    isMarketplaceSubmenuOpen = true;
                } else if (category.equals("equipment") && !isMaterielsSubmenuOpen) {
                    openSubmenu(materielsSubmenu, materielsArrow);
                    isMaterielsSubmenuOpen = true;
                } else if (category.equals("training") && !isFormationsSubmenuOpen) {
                    openSubmenu(formationsSubmenu, formationsArrow);
                    isFormationsSubmenuOpen = true;
                } else if (category.equals("applications") && !isCandidatureSubmenuOpen) {
                    openSubmenu(candidatureSubmenu, candidatureArrow);
                    isCandidatureSubmenuOpen = true;
                }
            }

            // Set this button as active
            setActiveButton(clickedButton);

            // Get the title for the view
            String title = getViewTitle(clickedButton);

            try {
                // Use Main controller to change the content
                Main.changeView(fxmlPath, title);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getViewTitle(Button button) {
        String title;
        if (button.getText() != null && !button.getText().isEmpty()) {
            title = button.getText();
        } else if (button.getTooltip() != null) {
            title = button.getTooltip().getText();
        } else {
            title = "Agriwise Admin";
        }

        // Special case for submenu items - prefix with parent menu name
        if (submenuParentMap.containsKey(button)) {
            Button parentButton = submenuParentMap.get(button);
            if (parentButton == gestionAgricoleBtn) {
                title = "Agricultural - " + title;
            } else if (parentButton == marketplaceBtn) {
                title = "Marketplace - " + title;
            } else if (parentButton == materielsBtn) {
                title = "Equipment - " + title;
            } else if (parentButton == formationsBtn) {
                title = "Training - " + title;
            } else if (parentButton == candidatureBtn) {
                title = "Applications - " + title;
            }
        }

        return title;
    }

    private void clearAllActiveStates() {
        // Clear active state from all possible buttons
        for (Button btn : buttonPathMap.keySet()) {
            btn.getStyleClass().remove("active");
        }

        // Also clear from any parent buttons that might be active
        for (Button parentBtn : new Button[]{gestionAgricoleBtn, marketplaceBtn, materielsBtn, formationsBtn, candidatureBtn}) {
            parentBtn.getStyleClass().remove("active");
            Button parentPair = buttonPairs.get(parentBtn);
            if (parentPair != null) {
                parentPair.getStyleClass().remove("active");
            }
        }
    }

    private void restoreActiveSubmenuState() {
        if (currentActiveCategory != null) {
            switch (currentActiveCategory) {
                case "agricultural":
                    if (isGestionAgricoleSubmenuOpen) {
                        gestionAgricoleSubmenu.setVisible(true);
                        gestionAgricoleSubmenu.setManaged(true);
                        gestionAgricoleArrow.setRotate(180);
                    }
                    break;
                case "marketplace":
                    if (isMarketplaceSubmenuOpen) {
                        marketplaceSubmenu.setVisible(true);
                        marketplaceSubmenu.setManaged(true);
                        marketplaceArrow.setRotate(180);
                    }
                    break;
                case "equipment":
                    if (isMaterielsSubmenuOpen) {
                        materielsSubmenu.setVisible(true);
                        materielsSubmenu.setManaged(true);
                        materielsArrow.setRotate(180);
                    }
                    break;
                case "training":
                    if (isFormationsSubmenuOpen) {
                        formationsSubmenu.setVisible(true);
                        formationsSubmenu.setManaged(true);
                        formationsArrow.setRotate(180);
                    }
                    break;
                case "applications":
                    if (isCandidatureSubmenuOpen) {
                        candidatureSubmenu.setVisible(true);
                        candidatureSubmenu.setManaged(true);
                        candidatureArrow.setRotate(180);
                    }
                    break;
            }
        }
    }

    public void setActiveViewByPath(String fxmlPath) {
        for (Map.Entry<Button, String> entry : buttonPathMap.entrySet()) {
            if (entry.getValue().equals(fxmlPath)) {
                Button button = entry.getKey();

                // Open relevant submenu if needed
                if (submenuParentMap.containsKey(button)) {
                    String category = buttonCategoryMap.get(button);
                    if (category.equals("agricultural") && !isGestionAgricoleSubmenuOpen) {
                        openSubmenu(gestionAgricoleSubmenu, gestionAgricoleArrow);
                        isGestionAgricoleSubmenuOpen = true;
                    } else if (category.equals("marketplace") && !isMarketplaceSubmenuOpen) {
                        openSubmenu(marketplaceSubmenu, marketplaceArrow);
                        isMarketplaceSubmenuOpen = true;
                    } else if (category.equals("equipment") && !isMaterielsSubmenuOpen) {
                        openSubmenu(materielsSubmenu, materielsArrow);
                        isMaterielsSubmenuOpen = true;
                    } else if (category.equals("training") && !isFormationsSubmenuOpen) {
                        openSubmenu(formationsSubmenu, formationsArrow);
                        isFormationsSubmenuOpen = true;
                    } else if (category.equals("applications") && !isCandidatureSubmenuOpen) {
                        openSubmenu(candidatureSubmenu, candidatureArrow);
                        isCandidatureSubmenuOpen = true;
                    }
                }

                setActiveButton(button);
                break;
            }
        }
    }

}