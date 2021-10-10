import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class UserController {
	
	private double minX;
	private double minY;
	private double width;
	private double height;
	final static  Semaphore semaphore = new Semaphore(1);

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button exitBottom;

    @FXML
    private Button positionsBottom;

    @FXML
    private Button profileBottom;
    
    @FXML
    private Button start;

    @FXML
    private Button activate;

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

    @FXML
    void setImage(ActionEvent event) {
    	GraphicsContext gc = canvasid.getGraphicsContext2D();
    	String imagePath = "view/coronaICON.png";
    	Image image = new Image(imagePath);
    	gc.drawImage(image, 10, 10, 100, 100);
    }

    @FXML
    void showPositions(ActionEvent event) {

    }

    @FXML
    void showProfile(ActionEvent event) {

    }
    
    @FXML
    void start(ActionEvent event) {
    	Client.command(Client.clientNo + " startPositions");
    }
    
	public void changeScene(ActionEvent event) throws IOException{
			Parent tableView;
			tableView = FXMLLoader.load(getClass().getResource("view/Sample.fxml"));
			Scene tableViewScene = new Scene(tableView,800,800);
			tableViewScene.getStylesheets().add("application.css");
			Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
			window.setScene(tableViewScene);
			window.show();
	}

    @FXML
    void initialize() {
        assert exitBottom != null : "fx:id=\"exitBottom\" was not injected: check your FXML file 'logued.fxml'.";
        assert positionsBottom != null : "fx:id=\"positionsBottom\" was not injected: check your FXML file 'logued.fxml'.";
        assert profileBottom != null : "fx:id=\"profileBottom\" was not injected: check your FXML file 'logued.fxml'.";
        assert activate != null : "fx:id=\"activate\" was not injected: check your FXML file 'logued.fxml'.";
        assert start != null : "fx:id=\"start\" was not injected: check your FXML file 'logued.fxml'.";
        assert canvasid != null : "fx:id=\"canvasid\" was not injected: check your FXML file 'logued.fxml'.";
		Timer timer = new Timer();
		TimerTask myTask = new TimerTask() {
			public void run() {
				Client.command(Client.clientNo + " authomatic " + Math.random()*400 + " " + Math.random()*400);
		    	GraphicsContext gc = canvasid.getGraphicsContext2D();
		    	gc.clearRect(minX, minY, 50, 50);
		    	gc.clearRect(minX, minY, 100, 100);
		    	String imagePath = "view/iconPerson.png";
		    	Image image = new Image(imagePath);
		    	String position[] = Client.command(Client.clientNo + " getPosition").split(" ");
		    	minX = Math.abs((int) Double.parseDouble(position[0]));
		    	minY = Math.abs((int) Double.parseDouble(position[1]));
		    	int radius = Client.radius(Client.clientNo);
		    	gc.fillOval(minX,minY,radius,radius);
		    	gc.drawImage(image,minX,minY,50,50);
		    	gc.setFill(Color.RED);
			}
		};
		timer.schedule(myTask,1000,1000);
    }

    @FXML
    void touched(MouseEvent event) throws InterruptedException {
    	semaphore.acquire();
    	GraphicsContext gc = canvasid.getGraphicsContext2D();
    	String imagePath;
    	if(Client.command(Client.clientNo + " getStatus").equals("0")) {
    		Client.command(Client.clientNo + " infected");
	    	gc.clearRect(10, 10, 100, 100);
	    	imagePath = "view/coronaICONnot.png";
    	}else {
    		Client.command(Client.clientNo + " healthy");
    		gc.clearRect(10, 10, 100, 100);
    		imagePath = "view/coronaICON.png";
    	}
    	semaphore.release();
    	Image image = new Image(imagePath);
    	gc.drawImage(image, 10, 10, 100, 100);
    }
}
