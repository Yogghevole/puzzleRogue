package view.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import model.service.RunService;

public class GameController {
    
    @FXML private StackPane mainGameArea; 
    @FXML private ImageView backgroundImageView;
    @FXML private StackPane modalContainer;
    @FXML private Label levelLabel;
    @FXML private Label difficultyLabel;
    @FXML private Button menuButton;
    @FXML private ImageView characterSpriteView;
    @FXML private ImageView enemySpriteView;
    @FXML private GridPane sudokuGridContainer;
    @FXML private HBox livesHBox;
    @FXML private HBox inputControlHBox;
    
    // Dipendenza
    private RunService runService;
    private static final int GRID_SIZE = 9;
    
    // Stato
    private Label[][] cellLabels = new Label[GRID_SIZE][GRID_SIZE]; 
    private Label[][][] noteLabels = new Label[GRID_SIZE][GRID_SIZE][GRID_SIZE];
    private VBox[][] sudokuCells = new VBox[GRID_SIZE][GRID_SIZE];
    private VBox selectedCellVBox = null;
    private int selectedRow = -1;
    private int selectedCol = -1;

    public void setRunService(RunService runService) {
        this.runService = runService;
    }
    
    @FXML
    public void initialize() {
        try {
            Font.loadFont(getClass().getResourceAsStream("/fonts/dark_font_Regular.tff"), 10);
            Font.loadFont(getClass().getResourceAsStream("/fonts/dark_font_DemiBold.tff"), 10);
            
            backgroundImageView.setImage(new Image(getClass().getResourceAsStream("/assets/backgrounds/levels/default_bg.png")));
            
        } catch (Exception e) {
            System.err.println("Errore nel caricamento del font o delle immagini: " + e.getMessage());
        }
        
        buildSudokuGrid();
        
        showCharacterSelectionModal(); 
    }
    
    private void buildSudokuGrid() {
        double cellSize = 55.0; 
        
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                VBox cellContainer = new VBox();
                cellContainer.setPrefSize(cellSize, cellSize);
                cellContainer.getStyleClass().add("sudoku-cell");
                cellContainer.getStyleClass().add("unselected-cell");

                if ((c + 1) % 3 == 0 && c != GRID_SIZE - 1) {
                    cellContainer.getStyleClass().add("heavy-border-right");
                }
                if ((r + 1) % 3 == 0 && r != GRID_SIZE - 1) {
                    cellContainer.getStyleClass().add("heavy-border-bottom");
                }
                
                GridPane noteGrid = new GridPane();
                noteGrid.setPrefSize(cellSize, cellSize);
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        int noteValue = i * 3 + j + 1;
                        Label noteLabel = new Label(String.valueOf(noteValue));
                        noteLabel.getStyleClass().add("note-label");
                        noteLabels[r][c][noteValue - 1] = noteLabel; 
                        
                        noteGrid.add(noteLabel, j, i);
                    }
                }
                
                Label valueLabel = new Label("");
                valueLabel.getStyleClass().add("initial-number"); 
                valueLabel.setVisible(false);

                StackPane contentPane = new StackPane(noteGrid, valueLabel);
                cellContainer.getChildren().add(contentPane);
                
                cellLabels[r][c] = valueLabel;
                sudokuCells[r][c] = cellContainer;
                
                final int finalR = r;
                final int finalC = c;
                cellContainer.setOnMouseClicked(e -> handleCellClick(finalR, finalC));
                
                sudokuGridContainer.add(cellContainer, c, r);
            }
        }
        
        buildInputControls();
    }
    
    private void buildInputControls() {
        for (int i = 1; i <= 9; i++) {
            Button numButton = new Button(String.valueOf(i));
            numButton.getStyleClass().add("input-number-button");
            final int value = i;
            numButton.setOnAction(e -> handleNumberInput(value));
            inputControlHBox.getChildren().add(numButton);
        }
        
        Button noteModeButton = new Button("NOTE");
        noteModeButton.setOnAction(e -> toggleNoteMode());
        inputControlHBox.getChildren().add(noteModeButton);
        
        Button clearButton = new Button("CLEAR");
        clearButton.setOnAction(e -> handleClearCell());
        inputControlHBox.getChildren().add(clearButton);
        
    }
    
    private void handleCellClick(int r, int c) {
        if (selectedCellVBox != null) {
            selectedCellVBox.getStyleClass().remove("selected-cell");
            selectedCellVBox.getStyleClass().add("unselected-cell");
        }
        
        VBox newSelection = sudokuCells[r][c];
        newSelection.getStyleClass().remove("unselected-cell");
        newSelection.getStyleClass().add("selected-cell");
        
        selectedCellVBox = newSelection;
        selectedRow = r;
        selectedCol = c;
        
    }
    
    private void handleNumberInput(int value) {
        if (selectedRow != -1 && selectedCol != -1) {
            System.out.println("Input: " + value + " in (" + selectedRow + "," + selectedCol + ")");
        }
    }

    private void toggleNoteMode() {
        System.out.println("Modalità Note attivata/disattivata.");
    }

    private void handleClearCell() {
        if (selectedRow != -1 && selectedCol != -1) {
            System.out.println("Cancellazione cella (" + selectedRow + "," + selectedCol + ")");
        }
    }
    
    @FXML
    private void handleMenuClick() {
        System.out.println("Mostra Menu Opzioni.");
    }
    
    private void showCharacterSelectionModal() {
        modalContainer.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        modalContainer.setVisible(true);
        System.out.println("Mostra Selezione Personaggio.");
    }

    public void updateSudokuView() {

    }
}