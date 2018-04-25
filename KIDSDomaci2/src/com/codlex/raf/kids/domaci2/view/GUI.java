package com.codlex.raf.kids.domaci2.view;

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

public class GUI {

	public static Button createButton(String string, Runnable onClick) {
		final Button button = new Button();
		button.setText(string);
		button.setPrefWidth(100);
		button.addEventHandler(MouseEvent.MOUSE_CLICKED,
		    new EventHandler<MouseEvent>() {
		        @Override public void handle(MouseEvent e) {
		        	onClick.run();
		        }
		});
		return button;
	}

	public static Object parseToType(final String value, Class<?> type) {

		if (type == Integer.class) {
			return Integer.parseInt(value);
		}

		if (type == Integer.class) {
			return Long.parseLong(value);
		}

		if (type == String.class) {
			return value;
		}

		return null;
	}
}
