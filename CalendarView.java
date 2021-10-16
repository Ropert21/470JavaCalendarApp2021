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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class CalendarView extends Application {

	private final List<TimeSlot> timeSlots = new ArrayList<>();
	LocalDate today = LocalDate.now();
	LocalDate currentDate = today;

	@Override
	public void start(Stage primaryStage) {
		GridPane calendarView = new GridPane();
		primaryStage.setTitle("Astronomical Calendar App");

		TextField yearEntry = new TextField();
		yearEntry.setMaxSize(80, 80);
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

		yearEntry.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ENTER) {
				int year = Integer.parseInt(yearEntry.getText());
				if (year > 2100) {
					yearEntry.setText("2100");
				} else if (year < 1900) {
					yearEntry.setText("1900");
				}
				year = Integer.parseInt(yearEntry.getText());
				currentDate = currentDate.withYear(year);
				System.out.println(currentDate);
				setMonth(calendarView, currentDate);
			}

		});

		yearEntry.setPromptText("1900-2100");
		yearEntry.setAlignment(Pos.CENTER_RIGHT);
		calendarView.add(yearEntry, 6, 1);

		// set text for yearEntry
		Text yearEntryText = new Text("Enter Year:");
		calendarView.add(yearEntryText, 5, 1);

		Button setButton = new Button();

		setButton.setOnMouseClicked(event -> {

		});

		setMonth(calendarView, currentDate);

		// Add space at top
		Rectangle r = new Rectangle(80, 80);
		r.setOpacity(0);
		calendarView.add(r, 1, 0);

		Month months[] = { Month.JANUARY, Month.FEBRUARY, Month.MARCH, Month.APRIL, Month.MAY, Month.JUNE,
				Month.JULY, Month.AUGUST, Month.SEPTEMBER, Month.OCTOBER, Month.NOVEMBER, Month.DECEMBER };

		// Create a combo box
		ComboBox<Month> monthsDropdown = new ComboBox<Month>(FXCollections.observableArrayList(months));
		monthsDropdown.getSelectionModel().select(currentDate.getMonthValue()-1);
		calendarView.add(monthsDropdown, 1, 1);
		
		monthsDropdown.setOnAction(event -> {
			currentDate = currentDate.withMonth(monthsDropdown.getValue().getValue());
			setMonth(calendarView, currentDate);
		});

		// headers:

		DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("E");
		LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
		LocalDate endOfWeek = startOfWeek.plusDays(6);

		for (LocalDate date = startOfWeek; !date.isAfter(endOfWeek); date = date.plusDays(1)) {
			Label label = new Label(date.format(dayFormatter));
			label.setPadding(new Insets(1));
			label.setTextAlignment(TextAlignment.CENTER);
			GridPane.setHalignment(label, HPos.CENTER);
			calendarView.add(label, date.getDayOfWeek().getValue(), 2);
		}

		ScrollPane scroller = new ScrollPane(calendarView);

		Scene scene = new Scene(scroller);
		scene.getStylesheets().add(getClass().getResource("calendar-view.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.show();

	}

	// Registers handlers on the time slot to manage selecting a range of slots in
	// the grid.

	// Utility method that checks if testSlot is "between" startSlot and endSlot
	// Here "between" means in the visual sense in the grid: i.e. does the time slot
	// lie in the smallest rectangle in the grid containing startSlot and endSlot
	//
	// Note that start slot may be "after" end slot (either in terms of day, or
	// time, or both).

	// The strategy is to test if the day for testSlot is between that for startSlot
	// and endSlot,
	// and to test if the time for testSlot is between that for startSlot and
	// endSlot,
	// and return true if and only if both of those hold.

	// Finally we note that x <= y <= z or z <= y <= x if and only if (y-x)*(z-y) >=
	// 0.

	// Class representing a time interval, or "Time Slot", along with a view.
	// View is just represented by a region with minimum size, and style class.

	// Has a selected property just to represent selection.

	public static class TimeSlot {

		private final LocalDate date;
		private final Pane view;

		public TimeSlot(LocalDate date) {
			this.date = date;

			view = new Pane();
			view.setMinSize(80, 80);
			view.getStyleClass().add("time-slot");

			Text t = new Text(10, 20, date.toString());
			view.getChildren().add(t);

			// TODO: Make action listener handle Swiss library on click
			view.setOnMouseClicked(event -> {
				System.out.println(date.toString());
			});

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

	public void setMonth(GridPane calendarView, LocalDate date) {
		for (TimeSlot t : timeSlots) {
			calendarView.getChildren().remove(t.getView());
		}

		timeSlots.clear();

		LocalDate startOfMonth = date.withDayOfMonth(1);
		LocalDate endOfMonth = date.withDayOfMonth(date.lengthOfMonth());
		int row = 3;

		for (LocalDate d = startOfMonth; !d.isAfter(endOfMonth); d = d.plusDays(1)) {

			TimeSlot timeSlot = new TimeSlot(d);
			timeSlots.add(timeSlot);

			calendarView.add(timeSlot.getView(), timeSlot.getDayOfWeek().getValue(), row);
			// calendarView.add

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
