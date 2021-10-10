import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class FXMLVistaController {

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private Button configuration;

	@FXML
	private Label label1;

	@FXML
	private TextField textArea;

	@FXML
	private PasswordField passArea;

	@FXML
	private Button logUser;

	@FXML
	private TextField sendT;

	@FXML
	private Button sendB;

	@FXML
	private ImageView mapa;

	@FXML
	void click(ActionEvent event) {
		int result = Client.startConf();
		if(result < 0) {
			label1.setText("Error");
		}else if(result == 0){
			label1.setText("Succeed");
			textArea.setVisible(false);
			passArea.setVisible(false);
			logUser.setVisible(false);
			sendB.setVisible(true);
			sendT.setVisible(true);
		}else {
			label1.setText("Introduzca usuario y contraseña");
		}
	}


	@FXML
	void loginUP(ActionEvent event) {
		String user = textArea.getText();
		String pwd = passArea.getText();
		int answer = Client.login(user,pwd);
		if(answer < 0) {
			label1.setText("# No hay configuración o Usuario y/o contraseña incorrecto.");
		}else {
			label1.setText("Usuario logueado con éxito");
			/*textArea.setVisible(false);
			passArea.setVisible(false);
			logUser.setVisible(false);
			sendB.setVisible(true);
			sendT.setVisible(true);*/
			try {
				if(user.equals("admin")) {
					changeScene(event,1);
				}else {
					changeScene(event,0);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	@FXML
	void sendC(ActionEvent event) {
		label1.setText(Client.command(Client.clientNo + " " + sendT.getText()));
	}

	void print(String text) {
		label1.setText(text);
	}

	public void changeScene(ActionEvent event, int id) throws IOException{
		/*Configuracion de cliente*/
		try {
			Parent tableView;
			if(id == 0) {
				tableView = FXMLLoader.load(getClass().getResource("view/logued.fxml"));
			}else {
				tableView = FXMLLoader.load(getClass().getResource("view/adminView.fxml"));
			}
			Scene tableViewScene = new Scene(tableView,400,400);
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
	void initialize() {
		assert configuration != null : "fx:id=\"configuration\" was not injected: check your FXML file 'sample.fxml'.";
		assert label1 != null : "fx:id=\"label1\" was not injected: check your FXML file 'sample.fxml'.";
		assert textArea != null : "fx:id=\"textArea\" was not injected: check your FXML file 'sample.fxml'.";
		assert passArea != null : "fx:id=\"passArea\" was not injected: check your FXML file 'sample.fxml'.";
		assert logUser != null : "fx:id=\"logUser\" was not injected: check your FXML file 'sample.fxml'.";
		assert sendT != null : "fx:id=\"sendT\" was not injected: check your FXML file 'sample.fxml'.";
		assert sendB != null : "fx:id=\"sendB\" was not injected: check your FXML file 'sample.fxml'.";
		assert mapa != null : "fx:id=\"mapa\" was not injected: check your FXML file 'sample.fxml'.";

	}
}