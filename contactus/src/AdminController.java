import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class AdminController {

	private int[] num;
	private double minX[];
	private double minY[];
	private double width;
	private double height;
	private boolean alarma = false;
	private CheckBox checkBoxes[];
	private VBox vbox;
	private int size;

	@FXML
	private Button exitBottom;

	@FXML
	private Button alarmsBottom;

	@FXML
	private Button positionsBottom;

	@FXML
	private Button usersBottom;

	@FXML
	private AnchorPane anchorpanel1;

	@FXML
	private AnchorPane anchorpanel2;

	@FXML
	private Canvas canvasid;

	@FXML
	void exit(ActionEvent event) {
		try {
			Client.command(Client.clientNo + " exit");
			changeScene(event);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void changeScene(ActionEvent event) throws IOException{
		try {
			Parent tableView;
			tableView = FXMLLoader.load(getClass().getResource("view/Sample.fxml"));
			Scene tableViewScene = new Scene(tableView,800,800);
			tableViewScene.getStylesheets().add("application.css");
			Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
			window.setScene(tableViewScene);
			window.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@FXML
	void show(ActionEvent event) {

	}

	@FXML
	void showAlarms(ActionEvent event) {
		if(alarma) {
			alarma = false;
			alarmsBottom.setText("Activar alarmas");
		}else {
			alarma = true;
			alarmsBottom.setText("Desactivar alarmas");
		}
	}

	@FXML
	void showPositions(ActionEvent event) {

	}

	@FXML
	void showUsers(ActionEvent event) {

	}
	
	

	@FXML
	void initialize() {
		assert exitBottom != null : "fx:id=\"exitBottom\" was not injected: check your FXML file 'adminView.fxml'.";
		assert alarmsBottom != null : "fx:id=\"alarmsBottom\" was not injected: check your FXML file 'adminView.fxml'.";
		assert positionsBottom != null : "fx:id=\"positionsBottom\" was not injected: check your FXML file 'adminView.fxml'.";
		assert usersBottom != null : "fx:id=\"usersBottom\" was not injected: check your FXML file 'adminView.fxml'.";
		assert anchorpanel1 != null : "fx:id=\"anchorpanel1\" was not injected: check your FXML file 'adminView.fxml'.";
		assert anchorpanel2 != null : "fx:id=\"anchorpanel2\" was not injected: check your FXML file 'adminView.fxml'.";
		assert canvasid != null : "fx:id=\"canvasid\" was not injected: check your FXML file 'adminView.fxml'.";

		vbox = new VBox();
		AnchorPane.setTopAnchor(vbox, 10.0);
		anchorpanel2.getChildren().clear(); 
		anchorpanel2.getChildren().add(vbox);
		
	      Thread thread = new Thread(new Runnable() {

	            @Override
	            public void run() {
	                Runnable updater = new Runnable() {

	                    @Override
	                    public void run() {
	                    	vbox.getChildren().clear();
	        				size = Integer.parseInt(Client.command(Client.clientNo + " getSesions")) + 1; //Guardamos el número de sesiones activas en la base de datos, ejemplo 4 clientes
	        				num = new int[size];
	        				double olDminX[] = minX;
	        				double olDminY[] = minY;
	        				minX = new double[size];
	        				minY = new double[size];
	        				CheckBox[] tempCheckBoxes = checkBoxes;
	        				checkBoxes = new CheckBox[size];
	        				
	        				for(int i = 1; i < size; i++) {
	        					if(i-1 < olDminX.length) {
	        						minX[i-1] = olDminX[i-1];
	        						minY[i-1] = olDminY[i-1];
	        					}
	        					if(i != Client.clientNo) {
	        						if(tempCheckBoxes[i] != null) {
	        							if(tempCheckBoxes[i].getText().equals(Client.command(Client.clientNo + " getUser " + i) + " " + i)) {
		        							checkBoxes[i] = tempCheckBoxes[i];
	        							}else if(!Client.command(Client.clientNo + " getUser " + i).equals("")){
	        								checkBoxes[i] = new CheckBox(Client.command(Client.clientNo + " getUser " + i) + " " + i); //Creamos un checkbox para cada cliente que contendrá el nombre de usuario y el identificador de cliente
	        							}
	        						}else if(!Client.command(Client.clientNo + " getUser " + i).equals("")){
		        						checkBoxes[i] = new CheckBox(Client.command(Client.clientNo + " getUser " + i) + " " + i); //Creamos un checkbox para cada cliente que contendrá el nombre de usuario y el identificador de cliente
	        						}
	        						if(checkBoxes[i]!=null) {
		        						vbox.getChildren().add(checkBoxes[i]); //Añadimos el checkbox al Vbox
	        						}
	        					}
	        					if(i != Client.clientNo) {
	        						GraphicsContext gcBoxes = canvasid.getGraphicsContext2D();
	        						if(checkBoxes[i]!=null) {
		        						insert(i,gcBoxes,checkBoxes[i]); //Dibujamos para cada chyeckbox si está marcado, la imagen del personaje en las coordenadas X e Y, así como si existe alarma
	        						}
	        					}
	        				}
	                    }
	                };

	                while (true) {
	                    try {
	                        Thread.sleep(1000);
	                    } catch (InterruptedException ex) {
	                    }

	                    // UI update is run on the Application thread
	                    Platform.runLater(updater);
	                }
	            }

	        });
	        // don't let thread prevent JVM shutdown
	        thread.setDaemon(true);
	        thread.start();
	    }

	private void insert(int n,GraphicsContext gc,CheckBox check) {
		Boolean control = false;

		if(check.isSelected()) {
			control = true;
		}

		if(control) {
			gc.clearRect(minX[n-1], minY[n-1], 50, 50);
			gc.clearRect(minX[n-1], minY[n-1], 100, 100);
			String imagePath = "view/iconPerson.png";
			Image image = new Image(imagePath);
			String position[] = Client.command(n + " getPosition").split(" ");
			System.out.println("prueba " + position[0] + position[1]);
			minX[n-1] = Math.abs((int) Double.parseDouble(position[0]));
			minY[n-1] = Math.abs((int) Double.parseDouble(position[1]));
			if(alarma) {
				int radius = Client.radius(n);
				gc.fillOval(minX[n-1],minY[n-1],radius,radius);
			}
			gc.drawImage(image,minX[n-1],minY[n-1],50,50);
			gc.setFill(Color.RED);
		}

	}

	private void Switch(int n) {
		// TODO Auto-generated method stub

	}
}
