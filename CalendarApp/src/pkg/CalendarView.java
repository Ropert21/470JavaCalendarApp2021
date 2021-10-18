package pkg;

import java.io.File;
import java.io.PrintWriter;
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
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CalendarView extends Application {

	private List<TimeSlot> timeSlots = new ArrayList<>();
	LocalDate currentDate = LocalDate.now();
	
	Label month = new Label(currentDate.getMonth().toString());
	Text longitudeText = new Text();
	Text latitudeText = new Text();
	Text elevationText = new Text();

	@Override
	public void start(Stage primaryStage) {
		GridPane calendarView = new GridPane();
		primaryStage.setTitle("Astronomical Calendar App");
		primaryStage.setResizable(false);
		primaryStage.sizeToScene();

		setMonth(calendarView, primaryStage, currentDate, new SwissEphDate(currentDate, -119.4960, 49.803, 334.0));
		setDayOfWeekHeaders(calendarView, currentDate);

		BorderPane header = new BorderPane();

		HBox hbox = setHBox(calendarView, primaryStage);
		header.setCenter(hbox);

		month.setFont(new Font("Arial", 50));

		BorderPane.setAlignment(month, Pos.CENTER);
		header.setBottom(month);

		header.getStyleClass().add("header");
		header.setBorder(new Border(
				new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

		// Main layout featuring calendar as the center
		BorderPane layout = new BorderPane();
		layout.setTop(header);
		layout.setCenter(calendarView);

		Scene scene = new Scene(layout);
		scene.getStylesheets().add(getClass().getResource("calendar-view.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.show();

	}

	// Set the HBox in the header which contains the buttons
	private HBox setHBox(GridPane calendarView, Stage primaryStage) {
		HBox hbox = new HBox();
		Button dateButton = new Button("Set month and year");

		// open new window
		dateButton.setOnMouseClicked(event -> {
			Stage dateWindow = new Stage();
			dateWindow.setTitle("Select Month and Year");
			dateWindow.initModality(Modality.WINDOW_MODAL);
			dateWindow.initOwner(primaryStage);

			// Year Entry textfield
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

			// Month drop-down
			Label monthEntryLabel = new Label("Select Month: ");
			Month months[] = { Month.JANUARY, Month.FEBRUARY, Month.MARCH, Month.APRIL, Month.MAY, Month.JUNE,
					Month.JULY, Month.AUGUST, Month.SEPTEMBER, Month.OCTOBER, Month.NOVEMBER, Month.DECEMBER };

			ComboBox<Month> monthsDropdown = new ComboBox<Month>(FXCollections.observableArrayList(months));
			monthsDropdown.getSelectionModel().select(currentDate.getMonthValue() - 1);

			// Ok Button
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
					//double[] positions = timeSlots.get(0).getSwissEphDate()
					setMonth(calendarView, primaryStage, currentDate, timeSlots.get(0).getSwissEphDate());
					dateWindow.close();

				} catch (NumberFormatException e) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setHeaderText("Invalid Entry");
					alert.setContentText("Please enter a year to continue");
					alert.show();
				}
			});

			// Cancel button
			Button cancelButton = new Button("Cancel");
			cancelButton.setOnMouseClicked(pressEvent -> {
				dateWindow.close();
			});

			// add children to pane
			GridPane setDatePane = new GridPane();
			setDatePane.setHgap(5);
			setDatePane.setVgap(5);
			setDatePane.setPadding(new Insets(10));

			setDatePane.add(yearEntryLabel, 1, 2);
			setDatePane.add(yearEntry, 2, 2);
			setDatePane.add(cancelButton, 1, 3);
			setDatePane.add(okButton, 2, 3);
			setDatePane.add(monthEntryLabel, 1, 1);
			setDatePane.add(monthsDropdown, 2, 1);

			dateWindow.setScene(new Scene(setDatePane));
			dateWindow.show();
		});

		hbox.setPadding(new Insets(15, 12, 15, 12));
		hbox.setSpacing(10);
		hbox.getStyleClass().add("hbox-header");

		Button geolocationButton = new Button("Set Geolocation");

		geolocationButton.setOnMouseClicked(pressEvent -> {
			Stage geolocationWindow = new Stage();
			geolocationWindow.setTitle("Select Month and Year");
			geolocationWindow.initModality(Modality.WINDOW_MODAL);
			geolocationWindow.initOwner(primaryStage);

			Label longitudeEntryLabel = new Label("Enter Longitude: ");
			TextField longitudeEntry = new TextField();
			longitudeEntry.setMaxSize(80, 80);
			longitudeEntry.setAlignment(Pos.CENTER_RIGHT);

			Label latitudeEntryLabel = new Label("Enter Latitude: ");
			TextField latitudeEntry = new TextField();
			latitudeEntry.setMaxSize(80, 80);
			latitudeEntry.setAlignment(Pos.CENTER_RIGHT);

			Label elevationEntryLabel = new Label("Enter Elevation: ");
			TextField elevationEntry = new TextField();
			elevationEntry.setMaxSize(80, 80);
			elevationEntry.setAlignment(Pos.CENTER_RIGHT);

			Button okButton = new Button("OK");

			okButton.setOnMouseClicked(okPressEvent -> {
				try {
					double longitude = Double.parseDouble(longitudeEntry.getText());
					double latitude = Double.parseDouble(latitudeEntry.getText());
					double elevation = Double.parseDouble(elevationEntry.getText());
					
					longitudeText.setText("Longitude: " + longitude);
					latitudeText.setText("Latitude: " +  latitude);
					elevationText.setText("Elevation: " + elevation + "m");

					for (TimeSlot ts : timeSlots) {
						ts.setSwissEphDate(new SwissEphDate(ts.getDate(), longitude, latitude, elevation));
					}
					geolocationWindow.close();
				} catch (NumberFormatException e) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setHeaderText("Invalid Entry");
					alert.setContentText("Please enter a valid longitude, latitude, and elevation to continue");
					alert.show();
				}
			});

			Button cancelButton = new Button("Cancel");
			cancelButton.setOnMouseClicked(cancelPressEvent -> {
				geolocationWindow.close();
			});

			GridPane setDatePane = new GridPane();
			setDatePane.setHgap(5);
			setDatePane.setVgap(5);
			setDatePane.setPadding(new Insets(10));

			setDatePane.add(longitudeEntryLabel, 1, 1);
			setDatePane.add(longitudeEntry, 2, 1);
			setDatePane.add(latitudeEntryLabel, 1, 2);
			setDatePane.add(latitudeEntry, 2, 2);
			setDatePane.add(elevationEntryLabel, 1, 3);
			setDatePane.add(elevationEntry, 2, 3);
			setDatePane.add(cancelButton, 1, 4);
			setDatePane.add(okButton, 2, 4);

			geolocationWindow.setScene(new Scene(setDatePane));
			geolocationWindow.show();

		});

		Button csvButton = new Button("Export Month");
		csvButton.setOnMouseClicked(cvsPressEvent -> {
			FileChooser fileChooser = new FileChooser();

			// Set extension filter for text files
			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
			fileChooser.getExtensionFilters().add(extFilter);

			// Show save file dialog
			File file = fileChooser.showSaveDialog(primaryStage);
			
			//Show dialog as its saving
			Alert csvAlert = new Alert(AlertType.INFORMATION);
			csvAlert.getButtonTypes().clear();
			csvAlert.setTitle("Saving file");
			csvAlert.setHeaderText(null);
			csvAlert.setContentText("Saving file, please wait...");

			csvAlert.show();
			try (PrintWriter writer = new PrintWriter(file)) {

				StringBuilder sb = new StringBuilder();
				sb.append("Date");
				sb.append(',');
				sb.append("SunriseTime");
				sb.append(',');
				sb.append("SunsetTime");
				sb.append(',');
				sb.append("MoonriseTime");
				sb.append(',');
				sb.append("MoonsetTime");
				sb.append(',');
				sb.append("MoonPhase");
				sb.append(',');
				sb.append("NextSolarDate");
				sb.append(',');
				sb.append("NextLunarDate");
				sb.append('\n');

				for (TimeSlot ts : timeSlots) {
					SwissEphDate s = ts.getSwissEphDate();
					sb.append(ts.getDate().toString());
					sb.append(',');
					sb.append(s.getSunriseTime());
					sb.append(',');
					sb.append(s.getSunsetTime());
					sb.append(',');
					sb.append(s.getMoonriseTime());
					sb.append(',');
					sb.append(s.getMoonsetTime());
					sb.append(',');
					sb.append(s.getMoonPhase());
					sb.append(',');
					sb.append(s.getNextSolarEclipse().toString());
					sb.append(',');
					sb.append(s.getNextLunarEclipse().toString());
					sb.append('\n');
				}

				//file now exists, so write to it and tell the user
				if (file != null) {
					writer.println(sb.toString());
					writer.close();
					//Button needed in order to close dialog
					csvAlert.getButtonTypes().add(ButtonType.OK);
					csvAlert.close();
					csvAlert = new Alert(AlertType.INFORMATION);
					csvAlert.setTitle("File Saved");
					csvAlert.setHeaderText(null);
					csvAlert.setContentText("File saved successfully!");
					csvAlert.showAndWait();
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		});
		
		//Create Space between buttons and position coordinates
        Region region1 = new Region();
        HBox.setHgrow(region1, Priority.ALWAYS);

		VBox vbox = new VBox();
		double[] position = timeSlots.get(0).getSwissEphDate().getPositions();
		longitudeText.setText("Longitude: " + position[0]);
		latitudeText.setText("Latitude: " + position[1]);
		elevationText.setText("Elevation: " + position[2] + "m");
		vbox.getChildren().addAll(longitudeText, latitudeText, elevationText);
		
		hbox.getChildren().addAll(dateButton, geolocationButton, csvButton, region1, vbox);
		return hbox;
	}

	//Mon    Tues    Wed    etc
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

	// Each square in the Calendar GUI is a Timeslot, stores it's own date, pane, and calculation
	public static class TimeSlot {
		private LocalDate date;
		private Pane view;
		private SwissEphDate SED;

		public TimeSlot(LocalDate date, Stage primaryStage, SwissEphDate s) {
			this.date = date;
			view = new Pane();
			view.setMinSize(80, 80);
			view.getStyleClass().add("time-slot");
			Text t = new Text(10, 20, date.getDayOfMonth() + "");
			
			double[] position = s.getPositions();
			SED = new SwissEphDate(date, position[0], position[1], position[2]);
			//SED = new SwissEphDate(date, -119.4960, 49.803, 334.0);
			ImageView moonPhaseImg = new ImageView(SED.getMoonPhaseImg());
			moonPhaseImg.setFitWidth(10);
			moonPhaseImg.setFitHeight(10);

			view.getChildren().addAll(t, moonPhaseImg);
			moonPhaseImg.relocate(60, 5);
			t.relocate(5, 0);

			view.setOnMouseClicked(event -> {
				Stage dateWindow = new Stage();
				dateWindow.setTitle(date.getMonth().toString() + " " + date.getDayOfMonth() + " " + date.getYear());
				dateWindow.initModality(Modality.WINDOW_MODAL);
				dateWindow.initOwner(primaryStage);

				// Calculate data for given date and display it
				Text sunriseText = new Text(10, 20, "Sunrise Time: " + SED.getSunriseTime());
				Text sunsetText = new Text(10, 20, "Sunset Time: " + SED.getSunsetTime());
				Text moonriseText = new Text(10, 20, "Moonrise Time: " + SED.getMoonriseTime());
				Text moonsetText = new Text(10, 20, "Moonset Time: " + SED.getMoonsetTime());
				Text nextSolarEclipseText = new Text(10, 20,
						"Next Solar Eclipse: " + SED.getNextSolarEclipse().toString());
				Text nextLunarEclipseText = new Text(10, 20,
						"Next Lunar Eclipse: " + SED.getNextLunarEclipse().toString());
				ImageView moonPhaseView = new ImageView(SED.getMoonPhaseImg());

				moonPhaseView.setFitWidth(50);
				moonPhaseView.setFitHeight(50);
				Tooltip.install(moonPhaseView, new Tooltip(SED.getMoonPhase()));

				GridPane datePane = new GridPane();
				datePane.setPadding(new Insets(37));
				datePane.setHgap(5);
				datePane.setVgap(5);
				datePane.getStyleClass().add("datePane");

				datePane.add(moonPhaseView, 1, 0);
				datePane.add(sunriseText, 1, 1);
				datePane.add(sunsetText, 1, 2);
				datePane.add(moonriseText, 1, 3);
				datePane.add(moonsetText, 1, 4);
				datePane.add(nextSolarEclipseText, 1, 5);
				datePane.add(nextLunarEclipseText, 1, 6);

				dateWindow.setScene(new Scene(datePane));
				dateWindow.show();
			});

		}

		public void setSwissEphDate(SwissEphDate s) {
			SED = s;
		}

		public SwissEphDate getSwissEphDate() {
			return SED;
		}

		public LocalDate getDate() {
			return date;
		}

		public DayOfWeek getDayOfWeek() {
			return date.getDayOfWeek();
		}

		public Node getView() {
			return view;
		}

	}

	public void setMonth(GridPane calendarView, Stage primaryStage, LocalDate date, SwissEphDate s) {
		for (TimeSlot t : timeSlots) {
			calendarView.getChildren().remove(t.getView());
		}
		timeSlots.clear();

		LocalDate startOfMonth = date.withDayOfMonth(1);
		LocalDate endOfMonth = date.withDayOfMonth(date.lengthOfMonth());
		int row = 3;

		for (LocalDate d = startOfMonth; !d.isAfter(endOfMonth); d = d.plusDays(1)) {
			TimeSlot timeSlot = new TimeSlot(d, primaryStage, s);
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
