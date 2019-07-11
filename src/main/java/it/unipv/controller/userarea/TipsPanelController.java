package it.unipv.controller.userarea;

import it.unipv.controller.common.IUserReservedAreaTrigger;
import it.unipv.db.*;
import it.unipv.dao.MovieDao;
import it.unipv.dao.PrenotationDao;
import it.unipv.dao.MovieDaoImpl;
import it.unipv.dao.PrenotationDaoImpl;
import it.unipv.model.Movie;
import it.unipv.model.MovieStatusTYPE;
import it.unipv.model.User;
import it.unipv.model.Prenotation;
import it.unipv.utils.ApplicationUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Controller di resources/fxml/userarea/TipsPanel.fxml
 * Questa classe viene utilizzata per mostrare le locandine dei film suggeriti all'utente:
 *     si basa sui tre generi più visti dell'utente e mostra i film, attualmente programmati, appartententi a quei generi
 */
public class TipsPanelController {

    private User loggedUser;
    private static int rowCount = 0;
    private static int columnCount = 0;
    private static int columnMax = 2;
    private List<Movie> fullMovieList = new ArrayList<>();
    private List<Movie> seenMovies = new ArrayList<>();
    private List<Movie> movies = new ArrayList<>();
    private GridPane grigliaFilm = new GridPane();
    private MovieDao movieDao;
    private PrenotationDao prenotationDao;
    private IUserReservedAreaTrigger areaRiservataController;
    @FXML private ScrollPane tipsPanel;
    @FXML private Label welcomeLabel;

    /**
     * Metodo principale del controller, deve essere chiamato all'inizializzazione della classe.
     * @param areaRiservataController -> serve per segnalare all'area riservata le operazioni effettuate
     * @param loggedUser -> l'utente che ha effettuato l'accesso al sistema
     * @param dbConnection -> la connessione al database utilizzata per istanziare PrenotationDaoImpl
     */
    public void init(IUserReservedAreaTrigger areaRiservataController, User loggedUser, DBConnection dbConnection) {
        this.movieDao = new MovieDaoImpl(dbConnection);
        this.prenotationDao = new PrenotationDaoImpl(dbConnection);
        this.loggedUser = loggedUser;
        this.areaRiservataController = areaRiservataController;
        columnMax = getColumnMaxFromPageWidth(tipsPanel.getScene().getWindow().getWidth());
        createUI();
        checkPageDimension();
    }

    private void createUI() {
        areaRiservataController.triggerStartStatusEvent("Carico suggerimenti in base ai film visti da " + loggedUser.getNome() + "...");
        Platform.runLater(() -> {
            initFullMovieList();
            initMovieList();
            createMovieGrid();
        });
        areaRiservataController.triggerEndStatusEvent("Suggerimenti per " + loggedUser.getNome() + " correttamente caricati!");
    }

    private void initFullMovieList() {
        fullMovieList = movieDao.retrieveCompleteMovieList(130,0,true,true);
    }

    //Inizializzo la lista dei film che saranno poi effettivamente mostrati, a partire dalla lista completa (fullMovieList)
    private void initMovieList() {
        List<String> topThreeSeenGenres = getTopThreeSeenGenres();
        for(String s : topThreeSeenGenres) {
            for(Movie m : fullMovieList) {
                if(m.getGenere().toLowerCase().contains(s.trim().toLowerCase()) && m.getStatus().equals(MovieStatusTYPE.AVAILABLE)) {
                    if(!movies.contains(m) && !seenMovies.contains(m)) {
                        movies.add(m);
                    }
                }
            }
        }
    }

    //Metodo che ricava i tre generi più visti dall'utente su cui basare i suggerimenti;
    private List<String> getTopThreeSeenGenres() {
        initSeenMovies(initPrenotationList());
        List<String> genres = new ArrayList<>();
        List<String> res = new ArrayList<>();

        for(Movie m : seenMovies) {
            genres.add(m.getGenere());
        }

        genres = ApplicationUtils.splitter(genres, ",");

        int max = genres.size()> 3 ? 3 : genres.size();
        Map.Entry<String, Integer> mostRepeated;
        for(int i=0; i<max; i++) {
            mostRepeated = getMostRepeatedWordInList(genres);
            for(int j=0; j<mostRepeated.getValue(); j++) {
                genres.remove(mostRepeated.getKey());
            }
            res.add(mostRepeated.getKey());
        }

        Collections.sort(res);
        return res;
    }

    private void initSeenMovies(List<Prenotation> prenotations) {
        for(Movie m : fullMovieList) {
            for(Prenotation p : prenotations) {
                if(m.getCodice().equalsIgnoreCase(p.getCodiceFilm()) && ApplicationUtils.checkIfDateIsPassed(p.getGiornoFilm())) {
                    if(!seenMovies.contains(m)) {
                        seenMovies.add(m);
                    }
                }
            }
        }
    }

    private List<Prenotation> initPrenotationList() {
        List<Prenotation> x = prenotationDao.retrievePrenotationList();
        List<Prenotation> prenotations = new ArrayList<>();
        for(Prenotation p : x) {
            if(p.getNomeUtente().equalsIgnoreCase(loggedUser.getNome())) {
                prenotations.add(p);
            }
        }
        Collections.sort(prenotations);
        return prenotations;
    }

    //Metodo che ricava la parola più ricorrente in una lista di stringhe
    private Map.Entry<String, Integer> getMostRepeatedWordInList(List<String> test) {
        Map<String, Integer> stringsCount = new HashMap<>();

        for(String string: test) {
            Integer count = stringsCount.get(string);
            if(count == null)  { count = 0; }
            count++;
            stringsCount.put(string,count);
        }

        Map.Entry<String,Integer> mostRepeated = null;
        for(Map.Entry<String, Integer> e: stringsCount.entrySet()) {
            if(mostRepeated == null || mostRepeated.getValue()<e.getValue())
                mostRepeated = e;
        }

        return mostRepeated;
    }

    //Metodo che si occupa di creare la griglia dei film suggeriti
    private void createMovieGrid() {
        grigliaFilm.getChildren().clear();
        if(movies.size()>0) {
            welcomeLabel.setText("Ecco una lista di film che potrebbero interessarti!");
            for (Movie movie : movies) {
                createViewFromMoviesList(movie);
            }
        } else {
            welcomeLabel.setText("Ci dispiace, non è stato possibile trovare alcun suggerimento.");
        }

        initRowAndColumnCount();
    }

    private void initRowAndColumnCount() {
        rowCount=0;
        columnCount=0;
    }

    //Metodo che definisce la singola cella della griglia, contenente locandina e nome del film
    private void createViewFromMoviesList(Movie movie) {
        Label nomeFilmLabel = new Label(StringUtils.abbreviate(movie.getTitolo(), 17));
        if(movie.getTitolo().length()>17) {
            nomeFilmLabel.setTooltip(new Tooltip(movie.getTitolo()));
        }
        nomeFilmLabel.setFont(Font.font("system", FontWeight.BOLD, FontPosture.REGULAR, 20));
        nomeFilmLabel.setTextFill(Color.WHITE);

        grigliaFilm.setHgap(80);
        grigliaFilm.setVgap(80);

        ImageView posterPreview = new ImageView(movie.getLocandina());
        posterPreview.setFitWidth(130);

        AnchorPane pane = new AnchorPane();
        if(columnCount==columnMax) {
            columnCount=0;
            rowCount++;
        }
        grigliaFilm.add(pane, columnCount, rowCount);
        columnCount++;

        tipsPanel.setContent(grigliaFilm);
        GridPane.setMargin(pane, new Insets(15,0,5,15));

        posterPreview.setLayoutX(30);

        nomeFilmLabel.setLayoutX(posterPreview.getLayoutX());
        nomeFilmLabel.setLayoutY(posterPreview.getLayoutY()+200);
        pane.getChildren().addAll(posterPreview, nomeFilmLabel);
    }


    private void refreshUI() { createMovieGrid(); }

    private int getColumnMaxFromPageWidth(double width) {
        if(width<800) {
            return 2;
        } else if(width>800 && width<=1360) {
            return 3;
        } else if(width>1360 && width<=1600) {
            return 4;
        } else if(width>1600) {
            return 5;
        } else {
            return 6;
        }
    }

    private int temp = 0;
    private void checkPageDimension() {
        Platform.runLater(() -> {
            Stage stage = (Stage) tipsPanel.getScene().getWindow();
            stage.widthProperty().addListener(e -> {
                columnMax = getColumnMaxFromPageWidth(stage.getWidth());
                if (temp != columnMax) {
                    temp = columnMax;
                    refreshUI();
                }
            });
        });
    }
}
