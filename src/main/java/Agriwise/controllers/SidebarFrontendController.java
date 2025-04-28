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

public class SidebarFrontendController {

    // Main sidebar components
    @FXML
    private VBox sidebarExpanded;
    @FXML
    private VBox sidebarCollapsed;

    // Submenu components
    @FXML
    private VBox gestionAgricoleSubmenu;
    @FXML
    private FontAwesomeIconView gestionAgricoleArrow;

    @FXML
    private VBox marketplaceSubmenu;
    @FXML
    private FontAwesomeIconView marketplaceArrow;

    @FXML
    private VBox encheresSubmenu;
    @FXML
    private FontAwesomeIconView encheresArrow;

    // All buttons (declarations remain the same as before)
    @FXML private Button accueilBtn, accueilIconBtn;
    @FXML private Button profileBtn, profileIconBtn;
    @FXML private Button gestionAgricoleBtn, gestionAgricoleIconBtn;
    @FXML private Button parcelleBtn, cultureBtn, activiteBtn,rendementBtn;
    ;
    @FXML private Button marketplaceBtn, marketplaceIconBtn;
    @FXML private Button produitsBtn, panierBtn;
    @FXML private Button encheresBtn, encheresIconBtn;
    @FXML private Button terrainsBtn, candidatureBtn;
    @FXML private Button formationsBtn, formationsIconBtn;
    @FXML private Button materielsBtn, materielsIconBtn;
    @FXML private Button logoutBtn, logoutIconBtn;
    @FXML private Button toggleBtn, expandBtn;

    private boolean isExpanded = true;
    private boolean isGestionAgricoleSubmenuOpen = false;
    private boolean isMarketplaceSubmenuOpen = false;
    private boolean isEncheresSubmenuOpen = false;

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
        linkButtonPairs();
        populateSubmenus();

        // Set default view to Accueil
        setActiveButton(accueilBtn);
    }

    private void setupNavigationPaths() {
        // Configure path for Accueil
        buttonPathMap.put(accueilBtn, "/Agriwise/views/AccueilView.fxml");
        buttonPathMap.put(accueilIconBtn, "/Agriwise/views/AccueilView.fxml");

        // Configure paths for menu buttons
        buttonPathMap.put(gestionAgricoleBtn, "/Agriwise/views/GestionAgricole/GestionAgricoleView.fxml");
        buttonPathMap.put(gestionAgricoleIconBtn, "/Agriwise/views/GestionAgricole/GestionAgricoleView.fxml");

        // Configure paths for submenu buttons under Gestion Agricole
        buttonPathMap.put(parcelleBtn, "/Agriwise/views/Parcelle/ParcelleView.fxml");
        buttonPathMap.put(cultureBtn, "/Agriwise/views/Culture/CultureView.fxml");
        buttonPathMap.put(activiteBtn, "/Agriwise/views/Activite/ActiviteView.fxml");
        buttonPathMap.put(rendementBtn, "/Agriwise/views/Parcelle/RendementView.fxml");


        // Configure paths for Marketplace
        buttonPathMap.put(marketplaceBtn, "/Agriwise/views/Marketplace/MarketplaceView.fxml");
        buttonPathMap.put(marketplaceIconBtn, "/Agriwise/views/Marketplace/MarketplaceView.fxml");

        // Configure paths for Marketplace submenu
        buttonPathMap.put(produitsBtn, "/Agriwise/views/Produit/product_list.fxml");
        buttonPathMap.put(panierBtn, "/Agriwise/views/Marketplace/PanierView.fxml");

        // Configure paths for Enchères
        buttonPathMap.put(encheresBtn, "/Agriwise/views/Encheres/EncheresView.fxml");
        buttonPathMap.put(encheresIconBtn, "/Agriwise/views/Encheres/EncheresView.fxml");

        // Configure paths for Enchères submenu
        buttonPathMap.put(terrainsBtn, "/Agriwise/views/Encheres/TerrainsView.fxml");
        buttonPathMap.put(candidatureBtn, "/Agriwise/views/Encheres/CandidatureView.fxml");

        // Configure paths for other buttons
        buttonPathMap.put(profileBtn, "/Agriwise/views/Utilisateur/ProfileScene.fxml");
        buttonPathMap.put(profileIconBtn, "/Agriwise/views/Utilisateur/ProfileScene.fxml");

        buttonPathMap.put(formationsBtn, "/Agriwise/views/Formation/ListFormationsFront.fxml");
        buttonPathMap.put(formationsIconBtn, "/Agriwise/views/Formation/ListFormationsFront.fxml");

        buttonPathMap.put(materielsBtn, "/Agriwise/views/Materiels/MaterielsView.fxml");
        buttonPathMap.put(materielsIconBtn, "/Agriwise/views/Materiels/MaterielsView.fxml");

        buttonPathMap.put(logoutBtn, "/Agriwise/views/Auth/LoginView.fxml");
        buttonPathMap.put(logoutIconBtn, "/Agriwise/views/Auth/LoginView.fxml");
    }

    private void setupCategoryMaps() {
        // (Same as before)
        buttonCategoryMap.put(accueilBtn, "accueil");
        buttonCategoryMap.put(accueilIconBtn, "accueil");

        buttonCategoryMap.put(gestionAgricoleBtn, "gestion_agricole");
        buttonCategoryMap.put(gestionAgricoleIconBtn, "gestion_agricole");

        buttonCategoryMap.put(parcelleBtn, "gestion_agricole");
        buttonCategoryMap.put(cultureBtn, "gestion_agricole");
        buttonCategoryMap.put(activiteBtn, "gestion_agricole");
        buttonCategoryMap.put(rendementBtn, "gestion_agricole");


        submenuParentMap.put(parcelleBtn, gestionAgricoleBtn);
        submenuParentMap.put(cultureBtn, gestionAgricoleBtn);
        submenuParentMap.put(activiteBtn, gestionAgricoleBtn);
        submenuParentMap.put(rendementBtn, gestionAgricoleBtn);

        buttonCategoryMap.put(marketplaceBtn, "marketplace");
        buttonCategoryMap.put(marketplaceIconBtn, "marketplace");

        buttonCategoryMap.put(produitsBtn, "marketplace");
        buttonCategoryMap.put(panierBtn, "marketplace");

        submenuParentMap.put(produitsBtn, marketplaceBtn);
        submenuParentMap.put(panierBtn, marketplaceBtn);

        buttonCategoryMap.put(encheresBtn, "encheres");
        buttonCategoryMap.put(encheresIconBtn, "encheres");

        buttonCategoryMap.put(terrainsBtn, "encheres");
        buttonCategoryMap.put(candidatureBtn, "encheres");

        submenuParentMap.put(terrainsBtn, encheresBtn);
        submenuParentMap.put(candidatureBtn, encheresBtn);
    }

    private void linkButtonPairs() {
        // (Same as before)
        linkButtonPair(accueilBtn, accueilIconBtn);
        linkButtonPair(gestionAgricoleBtn, gestionAgricoleIconBtn);
        linkButtonPair(marketplaceBtn, marketplaceIconBtn);
        linkButtonPair(encheresBtn, encheresIconBtn);
        linkButtonPair(profileBtn, profileIconBtn);
        linkButtonPair(formationsBtn, formationsIconBtn);
        linkButtonPair(materielsBtn, materielsIconBtn);
        linkButtonPair(logoutBtn, logoutIconBtn);
    }

    private void linkButtonPair(Button expandedButton, Button collapsedButton) {
        buttonPairs.put(expandedButton, collapsedButton);
        buttonPairs.put(collapsedButton, expandedButton);
    }

    private void populateSubmenus() {
        allSubmenus.add(gestionAgricoleSubmenu);
        allSubmenus.add(marketplaceSubmenu);
        allSubmenus.add(encheresSubmenu);
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
        } else if (clickedButton == encheresBtn) {
            toggleSubmenuVisibility(encheresSubmenu, encheresArrow);
            isEncheresSubmenuOpen = !isEncheresSubmenuOpen;
        }
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

    @FXML
    private void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String fxmlPath = buttonPathMap.get(clickedButton);

        if (fxmlPath != null) {
            // Handle submenu parent expansion if needed
            if (submenuParentMap.containsKey(clickedButton)) {
                Button parentButton = submenuParentMap.get(clickedButton);
                String category = buttonCategoryMap.get(clickedButton);

                if (category.equals("gestion_agricole") && !isGestionAgricoleSubmenuOpen) {
                    openSubmenu(gestionAgricoleSubmenu, gestionAgricoleArrow);
                    isGestionAgricoleSubmenuOpen = true;
                } else if (category.equals("marketplace") && !isMarketplaceSubmenuOpen) {
                    openSubmenu(marketplaceSubmenu, marketplaceArrow);
                    isMarketplaceSubmenuOpen = true;
                } else if (category.equals("encheres") && !isEncheresSubmenuOpen) {
                    openSubmenu(encheresSubmenu, encheresArrow);
                    isEncheresSubmenuOpen = true;
                }
            }

            // Set this button as active
            setActiveButton(clickedButton);

            // Get the title for the view
            String title = getViewTitle(clickedButton);

            try {
                // Use FrontendController to change the content
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
            title = "Agriwise";
        }

        // Special case for submenu items - prefix with parent menu name
        if (submenuParentMap.containsKey(button)) {
            Button parentButton = submenuParentMap.get(button);
            if (parentButton == gestionAgricoleBtn) {
                title = "Gestion Agricole - " + title;
            } else if (parentButton == marketplaceBtn) {
                title = "Marketplace - " + title;
            } else if (parentButton == encheresBtn) {
                title = "Enchères - " + title;
            }
        }

        return title;
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

    private void clearAllActiveStates() {
        // Clear active state from all possible buttons
        for (Button btn : buttonPathMap.keySet()) {
            btn.getStyleClass().remove("active");
        }

        // Also clear from any parent buttons that might be active
        for (Button parentBtn : new Button[]{gestionAgricoleBtn, marketplaceBtn, encheresBtn}) {
            parentBtn.getStyleClass().remove("active");
            Button parentPair = buttonPairs.get(parentBtn);
            if (parentPair != null) {
                parentPair.getStyleClass().remove("active");
            }
        }
    }


    @FXML
    private void toggleSidebar() {
        if (isExpanded) {
            // Collapse animation
            FadeTransition fadeOut = new FadeTransition(Duration.millis(150), sidebarExpanded);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                sidebarExpanded.setVisible(false);
                sidebarExpanded.setManaged(false);
                sidebarCollapsed.setVisible(true);
                sidebarCollapsed.setManaged(true);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(150), sidebarCollapsed);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeOut.play();
        } else {
            // Expand animation
            FadeTransition fadeOut = new FadeTransition(Duration.millis(150), sidebarCollapsed);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                sidebarCollapsed.setVisible(false);
                sidebarCollapsed.setManaged(false);
                sidebarExpanded.setVisible(true);
                sidebarExpanded.setManaged(true);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(150), sidebarExpanded);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();

                // Reopen submenu if needed
                if (currentActiveCategory != null) {
                    if (currentActiveCategory.equals("gestion_agricole") && !isGestionAgricoleSubmenuOpen) {
                        openSubmenu(gestionAgricoleSubmenu, gestionAgricoleArrow);
                        isGestionAgricoleSubmenuOpen = true;
                    } else if (currentActiveCategory.equals("marketplace") && !isMarketplaceSubmenuOpen) {
                        openSubmenu(marketplaceSubmenu, marketplaceArrow);
                        isMarketplaceSubmenuOpen = true;
                    } else if (currentActiveCategory.equals("encheres") && !isEncheresSubmenuOpen) {
                        openSubmenu(encheresSubmenu, encheresArrow);
                        isEncheresSubmenuOpen = true;
                    }
                }
            });
            fadeOut.play();
        }

        isExpanded = !isExpanded;
    }

    public void setActiveViewByPath(String fxmlPath) {
        for (Map.Entry<Button, String> entry : buttonPathMap.entrySet()) {
            if (entry.getValue().equals(fxmlPath)) {
                Button button = entry.getKey();

                // Open relevant submenu if needed
                if (submenuParentMap.containsKey(button)) {
                    String category = buttonCategoryMap.get(button);
                    if (category.equals("gestion_agricole") && !isGestionAgricoleSubmenuOpen) {
                        openSubmenu(gestionAgricoleSubmenu, gestionAgricoleArrow);
                        isGestionAgricoleSubmenuOpen = true;
                    } else if (category.equals("marketplace") && !isMarketplaceSubmenuOpen) {
                        openSubmenu(marketplaceSubmenu, marketplaceArrow);
                        isMarketplaceSubmenuOpen = true;
                    } else if (category.equals("encheres") && !isEncheresSubmenuOpen) {
                        openSubmenu(encheresSubmenu, encheresArrow);
                        isEncheresSubmenuOpen = true;
                    }
                }

                setActiveButton(button);
                break;
            }
        }
    }
}