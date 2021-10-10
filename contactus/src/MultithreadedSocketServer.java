import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.*;

public class MultithreadedSocketServer {
	public static void main(String[] args) throws Exception {
		try{
			ServerSocket server = new ServerSocket(8000);
			Connection con = DriverManager.getConnection("jdbc:sqlite:src/contactus.db");
			int counter=0;
			System.out.println("Servidor iniciado... ");
			while(true){
				//con.close();
				counter++;
				Socket serverClient = server.accept();  //server accept the client connection request
				System.out.println(" >> " + "Cliente No:" + counter + " iniciado!");
				con = DriverManager.getConnection("jdbc:sqlite:src/contactus.db");
				Server sct = new Server(serverClient,counter,con); //send  the request to a separate thread
				Thread hilo = new Thread(sct);
				hilo.start();
			}
		}catch(Exception e){
			System.out.println(e);
			
		}
	}
}
