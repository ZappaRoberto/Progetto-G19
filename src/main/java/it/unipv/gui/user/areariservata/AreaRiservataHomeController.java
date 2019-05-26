package it.unipv.gui.user.areariservata;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import it.unipv.gui.login.User;
import it.unipv.utils.ApplicationException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class AreaRiservataHomeController implements Initializable {

    @FXML private BorderPane mainPanel;
    @FXML private Label homeLabel, prenotationsLabel, seenMoviesLabel, tipsLabel, exitLabel;
    private List<Label> labels = new ArrayList<>();
    private User loggedUser;

    public void initialize(URL url, ResourceBundle rb) { }

    public void init(User loggedUser) {
        this.loggedUser = loggedUser;
        addLabelsToList();
        setOnMouseEnteredToLabels();
        setOnMouseExitedToLabels();
        initHomePane();
    }

    private void addLabelsToList() {
        labels.add(homeLabel);
        labels.add(prenotationsLabel);
        labels.add(seenMoviesLabel);
        labels.add(tipsLabel);
        labels.add(exitLabel);
    }

    private String openedPane = "";

    private void setOnMouseEnteredToLabels() {
        for(Label l : labels) {
            l.setOnMouseEntered(e -> {
                l.setCursor(Cursor.HAND);
                if(!openedPane.toLowerCase().equalsIgnoreCase(l.getText().toLowerCase())) {
                    l.setStyle("-fx-background-color:#efc677;");
                }
            });
        }

    }

    private void setOnMouseExitedToLabels() {
        for(Label l : labels) {
            l.setCursor(Cursor.DEFAULT);
            l.setOnMouseExited(e -> {
                if(!openedPane.toLowerCase().equalsIgnoreCase(l.getText().toLowerCase())) {
                    l.setStyle("-fx-background-color:transparent;");
                }
            });
        }
    }

    @FXML
    public void filterByOptions(MouseEvent event){
        Label label = (Label)event.getSource();

        switch(label.getText()) {
            case "HOME": {
                initHomePane();
                break;
            }

            case "PRENOTAZIONI":
                try {
                    if(!openedPane.equals("PRENOTAZIONI")) {
                        prenotationsLabel.setStyle("-fx-background-color:#db8f00");
                        setTransparentOtherLabels("PRENOTAZIONI");
                        mainPanel.getChildren().clear();
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/areariservata/PrenotationPanel.fxml"));
                        BorderPane prenotationPanel = loader.load();
                        PrenotationPanelController ppc = loader.getController();
                        prenotationPanel.prefHeightProperty().bind(mainPanel.heightProperty());
                        prenotationPanel.prefWidthProperty().bind(mainPanel.widthProperty());
                        ppc.init(loggedUser);
                        mainPanel.setCenter(prenotationPanel);
                        openedPane = "PRENOTAZIONI";
                    }
                } catch (IOException ex) {
                    throw new ApplicationException(ex);
                }
                break;

            case "FILM VISTI":
                //try {
                    if(!openedPane.equals("FILM VISTI")) {
                        seenMoviesLabel.setStyle("-fx-background-color:#db8f00");
                        setTransparentOtherLabels("FILM VISTI");
                        /*mainPanel.getChildren().clear();
                        AnchorPane movieListPanel = FXMLLoader.load(getClass().getResource("/fxml/manager/MovieListPanel.fxml"));
                        mainPanel.setCenter(movieListPanel);*/
                        openedPane = "FILM VISTI";
                    }
                /*} catch (IOException e) {
                    throw new ApplicationException(e);
                }*/
                break;

            case "SUGGERIMENTI":
               // try {
                    if(!openedPane.equals("SUGGERIMENTI")){
                        tipsLabel.setStyle("-fx-background-color:#db8f00");
                        setTransparentOtherLabels("SUGGERIMENTI");
                        /*mainPanel.getChildren().clear();
                        AnchorPane userListPanel = FXMLLoader.load(getClass().getResource("/fxml/manager/UserListPanel.fxml"));
                        mainPanel.setCenter(userListPanel);*/
                        openedPane = "SUGGERIMENTI";
                    }
                /*} catch (IOException e) {
                    throw new ApplicationException(e);
                }*/
                break;

            case "ESCI":
                Stage stage = (Stage) mainPanel.getScene().getWindow();
                stage.close();
                break;

            default:
                break;
        }
    }

    private void initHomePane() {
        try {
            if(!openedPane.equalsIgnoreCase("HOME")) {
                homeLabel.setStyle("-fx-background-color:#db8f00");
                setTransparentOtherLabels("HOME");
                mainPanel.getChildren().clear();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/areariservata/HomePanel.fxml"));
                BorderPane homePanel = loader.load();
                HomePanelController hpc = loader.getController();
                homePanel.prefHeightProperty().bind(mainPanel.heightProperty());
                homePanel.prefWidthProperty().bind(mainPanel.widthProperty());
                hpc.init(loggedUser);
                mainPanel.setCenter(homePanel);
                openedPane = "HOME";
            }
        } catch (IOException ex) {
            throw new ApplicationException(ex);
        }
    }

    private void setTransparentOtherLabels(String nameToExclude) {
        for(Label l : labels) {
            if(!l.getText().toLowerCase().equalsIgnoreCase(nameToExclude.toLowerCase())) {
                l.setStyle("-fx-background-color:transparent");
            }
        }
    }
}