import java.net.*;
import java.awt.Button;
import java.awt.Label;
import java.awt.TextArea;

import java.io.*;

import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Client extends Application{

	static Socket socket;
	static DataInputStream inStream;
	static DataOutputStream outStream;
	static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	static String clientMessage = "", serverMessage = "";

	static  int clientNo;
	final static  Semaphore semaphore = new Semaphore(1);

	@Override
	public void start(Stage primaryStage) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("view/Sample.fxml"));
			Scene scene = new Scene(root,800,800);
			scene.getStylesheets().add("application.css");
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main (String [] args){
		Application.launch(args);
	}

	protected static String command(String comando) {
		try {
			System.out.println(comando);
			semaphore.acquire();
			outStream.writeUTF(comando);
			outStream.flush();

			serverMessage = inStream.readUTF();
			System.out.println("# Mensaje recibido :");

			System.out.println(serverMessage);
			if(serverMessage.equals("failed")) {
				System.out.println("# Usted no está conectado.");
				login();
			}
			semaphore.release();
		}catch (Exception e) {
		}
		return serverMessage;
	}

	private static  void configuration() {
		/*Configuracion de cliente*/
		try {
			System.out.print("# Su identificador es: ");
			outStream.writeUTF("Start");
			outStream.flush();
			serverMessage = inStream.readUTF();
			System.out.println(serverMessage);
			clientNo = Integer.parseInt(serverMessage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static int login() {
		/*Configuracion de cliente*/
		try {
			/*Login*/
			System.out.println("# Iniciamos conexión.");
			serverMessage = inStream.readUTF();
			System.out.println("# Mensaje recibido.");
			if(!serverMessage.equals("OK")){
				return 1;
			}
			System.out.println("# Parece que todo ha ido bien.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	static int login(String user, String pwd) {
		/*Configuracion de cliente*/
		try {
			outStream.writeUTF(clientNo + " login " + user + " " + pwd);
			outStream.flush();
			serverMessage = inStream.readUTF();
			if(!serverMessage.equals("OK")){
				System.out.println("# No hay configuración o Usuario y/o contraseña incorrecto.");
				return -1;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("# Parece que todo ha ido bien.");
		return 0;
	}

	static int startConf() {
		int access = -1;
		System.out.println(
				"*********************************************\r\n" +
						"* Ejemplos de instrucciones                 *\r\n" +  
						"---------------------------------------------\r\n" +  	
						"* addUser [user] [passwd]                   *\r\n" + 
						"* login [user] [passwd]                     *\r\n" + 
						"* startPositions              	              *\r\n" + 
						"* stopPositions                             *\r\n" + 
						"* addPosition [id_User] [lat] [long]        *\r\n" + 
						"* listPositions [id_User] [Time]            *\r\n" + 
				"*********************************************\r\n");
		try {
			socket = new Socket("127.0.0.1",8000);
			inStream = new DataInputStream(socket.getInputStream());
			outStream = new DataOutputStream(socket.getOutputStream());

			System.out.println("*** Configurating the client ***");
			configuration();

			System.out.println("*** Software configurated ***\r\n"
					+ "*** Logging into the server ***");

			access = login();
			if(access == 0) {
				System.out.println("*** Logued SUCCESSFULL ***");
			}	
		}catch (Exception e) {
			return -1;
		}
		return access;
	}

	public static int radius(int idC) {
		return Integer.valueOf(command(idC + " radius"));
	}
}
