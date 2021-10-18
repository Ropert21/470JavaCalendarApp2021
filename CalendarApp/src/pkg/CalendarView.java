package pkg;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CalendarView extends Application {

	private List<TimeSlot> timeSlots = new ArrayList<>();
	LocalDate currentDate = LocalDate.now();
	Label month = new Label(currentDate.getMonth().toString());

	@Override
	public void start(Stage primaryStage) {
		GridPane calendarView = new GridPane();
		primaryStage.setTitle("Astronomical Calendar App");

		setMonth(calendarView, primaryStage, currentDate);
		setDayOfWeekHeaders(calendarView, currentDate);
	
		BorderPane header = new BorderPane();
		
		HBox hbox = setHBox(calendarView, primaryStage);
		header.setCenter(hbox);
		
		month.setFont(new Font("Arial", 50));
		
		BorderPane.setAlignment(month, Pos.CENTER);
		header.setBottom(month);
		
		header.getStyleClass().add("header");	
		header.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
		
		//Main layout featuring calendar as the center
	    BorderPane layout = new BorderPane();
	    layout.setTop(header);
		layout.setCenter(calendarView);
		
		Scene scene = new Scene(layout);
		scene.getStylesheets().add(getClass().getResource("calendar-view.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.show();

	}

	private HBox setHBox(GridPane calendarView, Stage primaryStage) {
		HBox hbox = new HBox();
		Button dateButton = new Button("Set month and year");

		//open new window
		dateButton.setOnMouseClicked(event -> {
			Stage dateWindow = new Stage();
			//TODO CHANGE THIS
			dateWindow.setTitle(currentDate.getMonth().toString() + " " + currentDate.getDayOfMonth() + " " + currentDate.getYear());
			dateWindow.initModality(Modality.WINDOW_MODAL);
			dateWindow.initOwner(primaryStage);
			
			//Year Entry textfield
			Label yearEntryLabel = new Label("Enter Year: ");	
			TextField yearEntry = new TextField();
			yearEntry.setMaxSize(80, 80);
			yearEntry.setPromptText("1900-2100");
			yearEntry.setText("" + currentDate.getYear());
			yearEntry.setAlignment(Pos.CENTER_RIGHT);
			yearEntry.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					if (!newValue.matches("\\d*")) {
						yearEntry.setText(newValue.replaceAll("[^\\d]", ""));
					}
					if (yearEntry.getText() != "") {

						if (yearEntry.getText().length() > 4) {
							String s = yearEntry.getText().substring(0, 4);
							yearEntry.setText(s);
						}
					}
				}
			});
			
			//Month drop-down
			Label monthEntryLabel = new Label("Select Month: ");
			Month months[] = { Month.JANUARY, Month.FEBRUARY, Month.MARCH, Month.APRIL, Month.MAY, Month.JUNE,
					Month.JULY, Month.AUGUST, Month.SEPTEMBER, Month.OCTOBER, Month.NOVEMBER, Month.DECEMBER };

			ComboBox<Month> monthsDropdown = new ComboBox<Month>(FXCollections.observableArrayList(months));
			monthsDropdown.getSelectionModel().select(currentDate.getMonthValue()-1);
			
			//Ok Button
			Button okButton = new Button("OK");
			okButton.setOnMouseClicked(pressEvent -> {
				try {
				int year = Integer.parseInt(yearEntry.getText());
				if (year > 2100) {
					year = 2100;
				} else if (year < 1900) {
					year = 1900;
				}
				currentDate = currentDate.withYear(year);
				currentDate = currentDate.withMonth(monthsDropdown.getValue().getValue());
				month.setText(currentDate.getMonth().toString());
				setMonth(calendarView, primaryStage, currentDate);
				dateWindow.close();
				
				} catch (NumberFormatException e) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setHeaderText("Invalid Entry");
					alert.setContentText("Please enter a year to continue");
					alert.show();
				}
			});
			
			//Cancel button
			Button cancelButton = new Button("Cancel");
			cancelButton.setOnMouseClicked(pressEvent -> {
				dateWindow.close();
			});
			
			//add children to pane
			GridPane datePane = new GridPane();
			datePane.add(yearEntryLabel, 1, 2);
			datePane.add(yearEntry, 2, 2);
			datePane.add(cancelButton, 1, 3);
			datePane.add(okButton, 2, 3);
			datePane.add(monthEntryLabel, 1, 1);
			datePane.add(monthsDropdown, 2, 1);
			
			datePane.setHgap(5);
			datePane.setVgap(5);
			datePane.setPadding(new Insets(10));
			
			dateWindow.setScene(new Scene(datePane));
			dateWindow.show();
		});
		
	    hbox.setPadding(new Insets(15, 12, 15, 12));
	    hbox.setSpacing(10);
	    hbox.setStyle("-fx-background-color: #336699;");
	    
	    Button geolocationButton = new Button("Set Geolocation");
	    
	    hbox.getChildren().addAll(dateButton, geolocationButton);
	    
		return hbox;
	}

	private void setDayOfWeekHeaders(GridPane calendarView, LocalDate date) {
		DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("E");
		LocalDate startOfWeek = date.minusDays(date.getDayOfWeek().getValue() - 1);
		LocalDate endOfWeek = startOfWeek.plusDays(6);

		for (LocalDate d = startOfWeek; !d.isAfter(endOfWeek); d = d.plusDays(1)) {
			Label label = new Label(d.format(dayFormatter));
			label.setPadding(new Insets(1));
			label.setTextAlignment(TextAlignment.CENTER);
			GridPane.setHalignment(label, HPos.CENTER);
			calendarView.add(label, d.getDayOfWeek().getValue(), 2);
		}
	}

	public static class TimeSlot {
		private LocalDate date;
		private Pane view;

		public TimeSlot(LocalDate date, Stage primaryStage) {
			this.date = date;
			view = new Pane();
			view.setMinSize(80, 80);
			view.getStyleClass().add("time-slot");
			Text t = new Text(10, 20, date.toString());
			view.getChildren().add(t);

			// TODO: Make action listener handle Swiss library on click
			view.setOnMouseClicked(event -> {
				SwissEphDate s = new SwissEphDate(date, -119.4960, 49.803, 334.0);
				System.out.println("Sunrise Time: " + s.getSunriseTime());
				System.out.println("Sunset Time: " + s.getSunsetTime());
				System.out.println("Moonrise Time: " + s.getMoonriseTime());
				System.out.println("Moonset Time: " + s.getMoonsetTime());
				
				Stage dateWindow = new Stage();
				//dateWindow.setTitle(date.getMonth().toString() + " " + currentDate.getDayOfMonth() + " " + currentDate.getYear());
				dateWindow.initModality(Modality.WINDOW_MODAL);
				dateWindow.initOwner(primaryStage);
				
				//Year Entry textfield
				Label yearEntryLabel = new Label("Enter Year: ");	
				TextField yearEntry = new TextField();
				yearEntry.setMaxSize(80, 80);
				yearEntry.setPromptText("1900-2100");
				//yearEntry.setText("" + currentDate.getYear());
				yearEntry.setAlignment(Pos.CENTER_RIGHT);
				
				
				GridPane datePane = new GridPane();
				datePane.add(yearEntryLabel, 1, 2);
				datePane.add(yearEntry, 2, 2);
				dateWindow.setScene(new Scene(datePane));
				dateWindow.show();
			});

		}

		public LocalDate getDate() {return date;}
		public DayOfWeek getDayOfWeek() {return date.getDayOfWeek();}
		public Node getView() {return view;}
	}
	
	public void setMonth(GridPane calendarView, Stage primaryStage, LocalDate date) {
		for (TimeSlot t : timeSlots) {
			calendarView.getChildren().remove(t.getView());
		}
		timeSlots.clear();
		
		LocalDate startOfMonth = date.withDayOfMonth(1);
		LocalDate endOfMonth = date.withDayOfMonth(date.lengthOfMonth());
		int row = 3;

		for (LocalDate d = startOfMonth; !d.isAfter(endOfMonth); d = d.plusDays(1)) {
			TimeSlot timeSlot = new TimeSlot(d, primaryStage);
			timeSlots.add(timeSlot);
			calendarView.add(timeSlot.getView(), timeSlot.getDayOfWeek().getValue(), row);

			// If it is Sunday, switch increase row count
			if (timeSlot.getDayOfWeek().getValue() == 7) {
				row++;
			}
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
