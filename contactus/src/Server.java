import java.net.Socket;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.concurrent.Semaphore;
import java.net.*;
import java.io.*;

public class Server implements Runnable {
	private Socket serverClient;
	private int clientNo;
	private static Connection con;
	final static  Semaphore semaphore = new Semaphore(1);

	Server(Socket inSocket,int counter,Connection con){
		serverClient = inSocket;
		clientNo = counter;
		this.con = con;
	}

	public void run(){
		System.out.println("# El hilo se ha abierto");
		try {
			boolean accept = true;
			DataInputStream inStream = new DataInputStream(serverClient.getInputStream());
			DataOutputStream outStream = new DataOutputStream(serverClient.getOutputStream());
			String clientMessage = "", serverMessage = "";
			String command[] = null;

			configuration(inStream,outStream,clientMessage,command,clientNo);
			//clientMessage = String.valueOf(clientNo);
			while(accept) {
				clientMessage = inStream.readUTF();
				command = clientMessage.split(" ");
				System.out.println(clientMessage);
				switch(command[1]) {
				case "addPosition":
					addPosition(command[2],command[3],command[4],checkLogin(Integer.valueOf(command[0])),inStream,outStream,clientMessage,command,clientNo,checkPosition(Integer.valueOf(command[0])));
					serverMessage = ">> Usuario "+nombreUsuario(checkId(command[0]))+" "  + command[0] + " Posici�n a�adida.";
					break;
				case "authomatic":
					addPosition(checkId(command[0]),command[2],command[3],checkLogin(Integer.valueOf(command[0])),inStream,outStream,clientMessage,command,clientNo,checkPosition(Integer.valueOf(command[0])));
					serverMessage = ">> Usuario "+nombreUsuario(checkId(command[0]))+" "  + command[0] + " Posici�n a�adida.";
					break;
				case "startPositions":
					serverMessage = ">> Usuario "+nombreUsuario(checkId(command[0]))+" "  + command[0] + " Iniciando almacenaje de posiciones: ";
					startPositions(checkLogin(Integer.valueOf(command[0])),Integer.valueOf(command[0]),inStream,outStream,clientMessage,command,clientNo);
					break;
				case "stopPositions":
					serverMessage = ">> Usuario "+nombreUsuario(checkId(command[0]))+" "  + command[0] + " No se admiten m�s posiciones: ";
					stopPositions(checkLogin(Integer.valueOf(command[0])),Integer.valueOf(command[0]),inStream,outStream,clientMessage,command,clientNo);
					break;
				case "addUser": //fe
					if(addUser(command[2],command[3],checkLogin(Integer.valueOf(command[0])),inStream,outStream,clientMessage,command,clientNo) == true) {
						serverMessage = ">> Usuario "+nombreUsuario(checkId(command[0]))+" "  + command[0] + " Aadiendo usuario";	
					}
					break;
				case "listPositions":
					serverMessage = ">> Usuario "+nombreUsuario(checkId(command[0]))+" " + command[0] + " Listando posiciones: \r\n " + listPositions(command[2],command[3],checkLogin(Integer.valueOf(command[0])),inStream,outStream,clientMessage,command,clientNo,checkAdmin(Integer.valueOf(command[0])));
					break;
				case "alarma":
					serverMessage = ">> Usuario "+nombreUsuario(checkId(command[0]))+" "  + command[0] + " mostrando alarmas: \r\n" + alarm(checkId(command[0]),checkLogin(Integer.valueOf(command[0]))) + "\r\n";
					break;
				case "listUsers":
					//serverMessage = ">> Usuario " + command[0] + " mostrando Usuarios: \r\n" + listUsers(checkLogin(Integer.valueOf(command[0])),checkAdmin(Integer.valueOf(command[0])),inStream,outStream,clientMessage,command,clientNo) + "\r\n";
					serverMessage = ">> Usuario "+ nombreUsuario(checkId(command[0])) +" "+ command[0] + " mostrando Usuarios: \r\n" + listUsers(checkLogin(Integer.valueOf(command[0])),checkAdmin(Integer.valueOf(command[0])),inStream,outStream,clientMessage,command,clientNo) + "\r\n";
					break;
				case "infected":
					serverMessage = ">> Usuario "+nombreUsuario(checkId(command[0])) + " "  + command[0] + " insertando usuario infectado: \r\n" + infected(checkLogin(Integer.valueOf(command[0])),inStream,outStream,clientMessage,command) + "\r\n";
					break;
				case "healthy":
					serverMessage = ">> Usuario "+nombreUsuario(checkId(command[0])) + " "  + command[0] + " insertando usuario curado: \r\n" + healthy(checkLogin(Integer.valueOf(command[0])),inStream,outStream,clientMessage,command) + "\r\n";
					break;
				case "closeContact":
					serverMessage = ">> Usuario "+nombreUsuario(checkId(command[0]))+" "  + command[0] +" "+ closeContact(checkId(command[0]),checkLogin(Integer.valueOf(command[0])),inStream,outStream,clientMessage,command)+ "\r\n";
					break;
				case "cuarentena":
					serverMessage = ">> Usuario "+nombreUsuario(checkId(command[0]))+" "+ cuarentena(checkId(command[0]),checkLogin(Integer.valueOf(command[0])),inStream,outStream,clientMessage,command)+ "\r\n";
					break;
				case "exit":
					serverMessage = ">> Usuario "+nombreUsuario(checkId(command[0]))+" success: "+ exit(Integer.valueOf(command[0]))+ "\r\n";
					break;
				case "FIN":
					serverMessage = ">> Usuario " + command[0] + " FIN";
					accept = false;
					break;
				case "getPosition":
					serverMessage = getPosition(checkId(command[0]),checkLogin(Integer.valueOf(command[0])),inStream,outStream);
					break;
				case "radius":
					serverMessage = getRadius(checkId(command[0]),checkLogin(Integer.valueOf(command[0])));
					break;
				case "getSesions":
					serverMessage = getSesions(checkLogin(Integer.valueOf(command[0])));
					break;
				case "getUser":
					if(checkLogin(Integer.valueOf(command[0]))){
						serverMessage = nombreUsuario(checkId(command[2]));
						System.out.println(serverMessage);
					}else {
						startLoguin(1,inStream,outStream,clientMessage,command,Integer.valueOf(command[0]));
					}
					break;
				case "getStatus":
					serverMessage = getStatus(checkLogin(Integer.valueOf(command[0])),checkId(command[0]));
					System.out.println("getStatus - > " + serverMessage);
					break;
				default:
					serverMessage = ">> Usuario " +nombreUsuario(checkId(command[0]))+" "+ command[0] + " Instrucci�n " + command[1] + " no reconocida";
					break;
				}
				outStream.writeUTF(serverMessage);
				outStream.flush();
			}
			inStream.close();
			outStream.close();
			serverClient.close();
		}catch (Exception e){

		}
	}

	/**
	 * 
	 * @param inStream
	 * @param outStream
	 * @param clientMessage
	 * @param command
	 * @param clientNo
	 * 
	 * ############## BLOQUE DE CONFIGURACI�N Y CONEXI�N CLIENTE - SERVIDOR ##############
	 */

	private synchronized void configuration(DataInputStream inStream,DataOutputStream outStream,String clientMessage,String[] command,int clientNo) {
		/*Configuracion*/
		System.out.println(clientMessage);
		try {
			System.out.println("# Esperando cliente: ");
			clientMessage = inStream.readUTF();
			outStream.writeUTF(String.valueOf(clientNo));
			outStream.flush();
			System.out.println("# Establecida conexi�n con el cliente: " + clientNo);
			startLoguin(0,inStream,outStream,clientMessage,command,clientNo);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param tosend
	 * @param inStream
	 * @param outStream
	 * @param clientMessage
	 * @param command
	 * @param clientNo
	 */

	private synchronized void startLoguin(int tosend, DataInputStream inStream,DataOutputStream outStream,String clientMessage,String[] command,int clientNo) {
		System.out.println("# Iniciamos login");
		int idCliente;
		/*Configuracion*/
		try {
			if(tosend == 1) {
				System.out.println("startLogin -> Mandamos mensaje failed");
				idCliente = Integer.valueOf(command[0]);
			}else {
				idCliente = clientNo;
			}
			/*login*/
			int idUser = 0;
			while(!checkLogin(idCliente)){
				System.out.println("startLogin -> # Imposible loguearse");
				outStream.writeUTF("Loguease con formato: login [Usuario] [Contrase�a]");
				outStream.flush();
				clientMessage = inStream.readUTF();
				command = clientMessage.split(" ");
				idUser = login(command[2],command[3],Integer.valueOf(command[0]));
			}
			String timeLog = "NAN";
			if(idUser!=0) {
				timeLog = saveLog(idUser);
			}else {
				System.out.println("# Usuario no encontrado");
			}
			outStream.writeUTF("OK");
			outStream.flush();
			System.out.println("# El cliente " + clientNo + " ha sido logueado con �xito " + timeLog);
			System.out.println("# Esperando instrucci�n: ");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param command
	 * @return
	 */

	private synchronized boolean checkLogin(int command) {
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Sesion WHERE id == '" + command + "'");
			int idUser = rs.getInt(1);
			rs.close();
			stmt.close();

			if(idUser > 0) {
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //Hacer a prueba de inyecciones de c�digo SQL
		return false;
	}

	/**
	 * 
	 * @param user -> Usuario
	 * @param passwd -> Contrase�a
	 * @param idClient -> Identificador del hilo
	 * @return
	 */

	private synchronized int login(String user, String passwd,int idClient) {
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM User WHERE email == '" + user + "' AND "
					+ "password == '" + passwd + "'"); //Hacer a prueba de inyecciones de c�digo SQL
			int numRow = rs.getInt(1);
			rs.close();
			if(numRow > 0) {
				System.out.println("Acceso permitido");
				ResultSet checkSesion = stmt.executeQuery("SELECT id FROM User WHERE email == '" + user + "'");
				int idUser = checkSesion.getInt(1);
				System.out.println("idUser: " + idUser);
				int count = 0;
				checkSesion = stmt.executeQuery("SELECT COUNT(id) FROM Sesion WHERE idUser == (SELECT id FROM User WHERE email =='" + user + "')");
				int existance = checkSesion.getInt(1);
				if(existance == 0){
					count = stmt.executeUpdate("INSERT INTO Sesion VALUES (" + idClient + "," + idUser + ",'" + 0 + "','" + 0 + "')");
					saveLog(idUser);
				}else {
					count = stmt.executeUpdate("DELETE FROM Sesion WHERE idUser == (SELECT id FROM User WHERE email == '" + user + "')");
				}
				System.out.println("success " + count);
				return idUser;
			}
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	private synchronized String saveLog(int idUser) {
		Calendar calendario = Calendar.getInstance();
		String timeLog = calendario.get(Calendar.DAY_OF_MONTH) + "-" + (calendario.get(Calendar.MONTH)+1) + "-" + calendario.get(Calendar.YEAR) + " " + calendario.get(Calendar.HOUR_OF_DAY) + ":" + calendario.get(Calendar.MINUTE) + ":" + calendario.get(Calendar.SECOND);
		System.out.println("saveLog -> Guardando conexi�n: ");
		int success;
		try {
			Statement stmt = con.createStatement();
			success = stmt.executeUpdate("INSERT INTO Log (idUser,time) VALUES ('" + idUser + "','" + timeLog + "')");			
			stmt.close();

			System.out.println("success "+ success);			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return timeLog;
	}

	/* ############## Bloque comprobar administrador######################*/
	private synchronized boolean checkAdmin(int command) {
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT type FROM User WHERE id == (SELECT idUser FROM Sesion WHERE id == " + command + ")");
			int admin = rs.getInt(1);
			//rs.close();
			stmt.close();

			if(admin > 0) {
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //Hacer a prueba de inyecciones de c�digo SQL
		return false;
	}

	/*#################### FUNCIONES #####################*/


	private synchronized String listUsers(boolean login, boolean admin, DataInputStream inStream,DataOutputStream outStream,String clientMessage,String[] command,int clientNo) {
		if(login && admin) {
			try {
				String servermessage = "";
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM User");
				while (rs.next()) {
					String email = rs.getString(2);
					String pass = rs.getString(3);
					servermessage += "email: " + email + " pass: " + pass + "\r\n";
				}
				rs.close();
				stmt.close();

				return servermessage;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(!login){
			startLoguin(1,inStream,outStream,clientMessage,command,clientNo);
		}
		return "Usted no es administrador u otro error";
	}

	/**
	 * 
	 * @param id_user -> Id del usuario
	 * @param lat -> Latitud
	 * @param lon -> Longitud
	 * @param time -> Tiempo
	 * @param login -> �Se ha iniciado sesi�n?
	 * @param inStream
	 * @param outStream
	 * @param clientMessage
	 * @param command
	 * @param clientNo -> Identificador del hilo
	 * @return 
	 */

	private synchronized boolean checkPosition(int id) {
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Sesion WHERE id == '" + id + "' AND record == '" + 1 + "'");
			int idUser = rs.getInt(1);
			rs.close();
			stmt.close();
			if(idUser > 0) {
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //Hacer a prueba de inyecciones de c�digo SQL
		return false;
	}

	private synchronized void addPosition(String id_user, String lat, String lon, boolean login,DataInputStream inStream,DataOutputStream outStream,String clientMessage,String[] command,int clientNo, boolean position){
		try {
			semaphore.acquire();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(login && position) {
			Calendar calendario = Calendar.getInstance();
			String timeLog = calendario.get(Calendar.DAY_OF_MONTH) + "-" + (calendario.get(Calendar.MONTH)+1) + "-" + calendario.get(Calendar.YEAR) + " " + calendario.get(Calendar.HOUR_OF_DAY) + ":" + calendario.get(Calendar.MINUTE) + ":" + calendario.get(Calendar.SECOND);
			try {
				Statement stmt = con.createStatement();
				int success = stmt.executeUpdate("INSERT INTO Location (idUser,lat,long,time) VALUES ("+ id_user + "," + lat + "," + lon + ",'" + timeLog + "')");
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(!login){
			startLoguin(1,inStream,outStream,clientMessage,command,clientNo);
		}
		semaphore.release();
	}

	/**
	 * 
	 * @param login -> �Se ha iniciado sesi�n?
	 * @param id -> Identificador de la sesi�n
	 * @param inStream
	 * @param outStream
	 * @param clientMessage
	 * @param command
	 * @param clientNo -> Identificador del hilo
	 */

	private synchronized void startPositions(boolean login, int id,DataInputStream inStream,DataOutputStream outStream,String clientMessage,String[] command,int clientNo) {
		if(login) {
			try {
				Statement stmt = con.createStatement();
				int success = stmt.executeUpdate("UPDATE Sesion SET record = 1 WHERE id == '" + id + "'");
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else {
			try {
				outStream.writeUTF("failed");
				outStream.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			startLoguin(1,inStream,outStream,clientMessage,command,clientNo);
		}
	}

	/**
	 * 
	 * @param login -> �Se ha iniciado sesi�n?
	 * @param id -> Identificador de sesi�n
	 * @param inStream
	 * @param outStream
	 * @param clientMessage
	 * @param command
	 * @param clientNo -> Identificador del hilo
	 */

	private synchronized void stopPositions(boolean login, int id,DataInputStream inStream,DataOutputStream outStream,String clientMessage,String[] command,int clientNo) {
		if(login) {
			try {
				Statement stmt = con.createStatement();
				int success = stmt.executeUpdate("UPDATE Sesion SET record = 0 WHERE id == '" + id + "'");
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else {
			startLoguin(1,inStream,outStream,clientMessage,command,clientNo);
		}
	}

	/**
	 * 
	 * @param user -> Usuario
	 * @param passwd -> Contrase�a
	 * @param login -> �Existe ya una sesi�n iniciado con ese usuario?
	 * @param inStream
	 * @param outStream
	 * @param clientMessage
	 * @param command
	 * @param clientNo -> Identificador del hilo
	 */

	private synchronized boolean addUser(String user, String passwd, boolean login,DataInputStream inStream,DataOutputStream outStream,String clientMessage,String[] command,int clientNo) {
		boolean res = false;
		if(login) {
			try {
				Statement stmt = con.createStatement();
				if(verificarDuplicidad(user) == false) {
					int success = stmt.executeUpdate("INSERT INTO User (email,password,type) VALUES ('" + user + "','" + passwd + "','0')");
				}
				stmt.close();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else {
			startLoguin(1,inStream,outStream,clientMessage,command,clientNo);
		}
		return res;
	}

	/**
	 * 
	 * @param user -> Usuario
	 * @param marca -> Marca de tiempo
	 * @param login -> �Se ha iniciado sesi�n?
	 * @param inStream
	 * @param outStream
	 * @param clientMessage
	 * @param command
	 * @param clientNo -> Identificador del hilo
	 */

	private synchronized String listPositions(String user, String marca, boolean login,DataInputStream inStream,DataOutputStream outStream,String clientMessage,String[] command,int clientNo, boolean admin) {
		if(login && admin) {
			Calendar calendario = Calendar.getInstance();
			String timeLog = calendario.get(Calendar.DAY_OF_MONTH) + "-" + (calendario.get(Calendar.MONTH)+1) + "-" + calendario.get(Calendar.YEAR) + " " + calendario.get(Calendar.HOUR_OF_DAY) + ":" + calendario.get(Calendar.MINUTE) + ":" + calendario.get(Calendar.SECOND);
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			try {
				long diff = 999999999;
				long second = 1000l;
				long minute = 60l * second;
				long hour = 60l * minute;

				String servermessage = "";
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM Location WHERE idUser == (SELECT id FROM User WHERE email = '" + user + "')");
				while (rs.next()) {
					int x = rs.getInt(1);
					String s = rs.getString(2);
					String lat = rs.getString(3);
					String lon = rs.getString(4);
					String time = rs.getString(5);
					try {
						// calculation
						diff = dateFormat.parse(timeLog).getTime() - dateFormat.parse(time).getTime();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(((diff%hour)/minute) < Integer.valueOf(marca)){
						servermessage += "id: " + x + "; id_Usuario: " + s +"; Latitud: " + lat + "; Longitud: " + lon + "; Marca_Time: " + time + "\r\n";
					}
				}
				rs.close();
				stmt.close();

				return servermessage;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(!login){
			startLoguin(1,inStream,outStream,clientMessage,command,clientNo);
		}
		return "No es usted administrador u otro error";
	}

	private synchronized boolean verificarDuplicidad(String email){
		boolean res = false;
		try{
			Statement stmt = con.createStatement(); 
			ResultSet rs = stmt.executeQuery("SELECT * FROM User WHERE email ='" + email + "'");
			if(rs.next() != false){
				res = true;  
			}
			rs.close();
			stmt.close();


		}catch(SQLException e){
			e.printStackTrace();
		}

		return res;
	}

	private synchronized String alarm(String id, boolean login) {
		String servermessage = "";
		try {
			Statement stmt = con.createStatement();
			int min = 100;
			ResultSet rs = stmt.executeQuery("SELECT * FROM Location WHERE idUser == '" + id + "' ORDER BY id DESC LIMIT 1");
			int x = rs.getInt(1);
			String s = rs.getString(2);
			String lat1 = rs.getString(3);
			String lon1 = rs.getString(4);
			String time = rs.getString(5);
			rs.close();
			stmt.close();
			servermessage = alarmInfecteds(id,x,s,lat1,lon1,time);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return servermessage;
	}

	private synchronized String alarmInfecteds(String id,int x,String s, String lat1, String lon1, String time) {
		String servermessage = "";
		Calendar calendario = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		Statement stmt;
		try {
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM Location WHERE idUser IN (SELECT id FROM User WHERE status == 1) AND idUser IS NOT '"+id+"'");
			while(rs.next()) {
				int x2 = rs.getInt(1);
				String s2 = rs.getString(2);
				String lat2 = rs.getString(3);
				String lon2 = rs.getString(4);
				String time2 = rs.getString(5);
				double diff = 999999;
				try {
					diff = dateFormat.parse(time).getTime() - dateFormat.parse(time2).getTime();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(diff/1000/60 < 2880 && diff/1000/60 > 0) {  //Cambio de 15 a 2880, para que así coja también contactos estrechos (actualización SPRINT 3)
					double dis = Math.sqrt(Math.pow((Double.valueOf(lat2) - Double.valueOf(lat1)),2) + Math.pow((Double.valueOf(lon2) - Double.valueOf(lon1)),2));
					if(dis <= 2) {
						servermessage += x + " " + x2 + " " + dis + " \r\n";
					}
				}
			}
			System.out.println(servermessage);

			if(!servermessage.equals("")) {
				String[] cadena = servermessage.split(" \r\n");
				for(int i = 0; i < cadena.length; i++) {
					System.out.println("Elemento: " + i + " Valor: " + cadena[i]);
					String[] elemento = cadena[i].split(" ");
					for(int j = 0; j < elemento.length-1; j++) {
						System.out.println("Elemento: " + i + " Valor: " + elemento[j]);
						rs = stmt.executeQuery("SELECT email from User WHERE id IN (SELECT idUser FROM Location WHERE id == '" + elemento[j] + "')");
						String email = rs.getString(1);
						servermessage += email + " ";
					}
					servermessage += (int) (Double.valueOf(elemento[2])*10) + "m \r\n";
				}
			}
			//servermessage = "";

			rs.close();
			stmt.close();

		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(servermessage == "") {
			return " No hay alarmas ";
		}else {
			return servermessage;
		}

	}

	private synchronized String infected(boolean login,DataInputStream inStream,DataOutputStream outStream,String clientMessage,String[] command) {
		if(login) {
			Calendar calendario = Calendar.getInstance();
			String timeLog = calendario.get(Calendar.DAY_OF_MONTH) + "-" + (calendario.get(Calendar.MONTH)+1) + "-" + calendario.get(Calendar.YEAR) + " " + calendario.get(Calendar.HOUR_OF_DAY) + ":" + calendario.get(Calendar.MINUTE) + ":" + calendario.get(Calendar.SECOND);
			try {
				Statement stmt = con.createStatement();
				int success = stmt.executeUpdate("UPDATE User SET status = '1', statusTime = '"+ timeLog +"' WHERE id IN (SELECT idUser FROM Sesion WHERE id =='" + command[0] + "')");
				stmt.close();
				if(success > 0) {
					return "Actualizado con �xito";
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		return "Failed";
	}

	private synchronized String healthy(boolean login,DataInputStream inStream,DataOutputStream outStream,String clientMessage,String[] command) {
		if(login) {
			Calendar calendario = Calendar.getInstance();
			String timeLog = calendario.get(Calendar.DAY_OF_MONTH) + "-" + (calendario.get(Calendar.MONTH)+1) + "-" + calendario.get(Calendar.YEAR) + " " + calendario.get(Calendar.HOUR_OF_DAY) + ":" + calendario.get(Calendar.MINUTE) + ":" + calendario.get(Calendar.SECOND);
			try {
				Statement stmt = con.createStatement();
				int success = stmt.executeUpdate("UPDATE User SET status = '0', statusTime = '"+ timeLog +"' WHERE id IN (SELECT idUser FROM Sesion WHERE id =='" + command[0] + "')");
				stmt.close();
				if(success > 0) {
					return "Actualizado con �xito";
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		return "Failed";
	}

	private synchronized String checkId(String clientNo) throws IOException {
		try {
			Statement stmt = con.createStatement();
			ResultSet ch = stmt.executeQuery("SELECT idUser FROM Sesion WHERE id == '" + clientNo + "'");
			int prueba = ch.getInt(1);
			if(prueba > 0) {
				ch.close();
				stmt.close();
				return String.valueOf(prueba);
			}
			ch.close();
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "0";
	}


	private synchronized String closeContact (String id, boolean login,DataInputStream inStream,DataOutputStream outStream,String clientMessage,String[] command) throws ParseException {
		String rslt="";
		String rsltUser ="";

		if(login) {

			try {
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM Location WHERE idUser = '"+id+"'");
				//Obtenemos las posiciones del Usuario logueado
				while(rs.next()) {
					int x2 = rs.getInt(1);
					String s2 = rs.getString(2);
					String lat2 = rs.getString(3);
					String lon2 = rs.getString(4);
					String time2 = rs.getString(5);
					//Si esa posici�n coincide con la de un infectado en un rango de 48h o 2m almacenamos la posici�n	
					rslt +=obtenerPosiciones2h(id,lat2,lon2,time2,lat2,lon2,time2);
					rsltUser += x2+" "+s2+" "+lat2+" "+lon2+" "+time2+"\n";

				}
				System.out.println(rslt);  //debug muestra posiciones en las que ha sido contacto estrecho
				System.out.println("---------------------------");


				//rs.close();
				//stmt.close();

			}catch(SQLException e) {
				e.printStackTrace();
			}

		}

		if(rslt.equals("")) {
			System.out.println("No es contacto estrecho"); //Por defecto 0 en BBDD de la tabla User

		}else {
			//System.out.println("Contato estrecho"); 
			//Poner a 1 en la BBDD

			String tiempos [] = rsltUser.split("\n");

			int horaColumna = 0; //Variable para coger las horas m�s tard�as
			int diaColumna = 0;
			int mesColumna = 0;
			String closeCTime = "";

			for(int i=0; i< tiempos.length; i++) {

				String [] aux = tiempos[i].split(" ");
				String aux2 = aux[4] +" "+ aux[5];
				// aux2 Nos dar� de reusltado un formato (dd-MM-yyyy HH:mm:ss)


				String [] aux3 = aux2.split(" ");  // aux3 separa d�as de horas
				String [] aux4 = aux3[1].split(":");  //Obtenemos hora
				int t = Integer.parseInt(aux4[0]) * 60*60*1000 + Integer.parseInt(aux4[1]) *60*1000 + Integer.parseInt(aux4[2]) *1000;
				String [] aux5 = aux3[0].split("-"); 
				int comprobarDia = Integer.parseInt(aux5[0]);
				int comprobarMes = Integer.parseInt(aux5[1]);

				if(comprobarMes >= mesColumna) {
					mesColumna = comprobarMes;
					closeCTime =  aux2; 
					if(comprobarDia > diaColumna) {
						diaColumna = comprobarDia;
						closeCTime =  aux2; 
						if(t > horaColumna) {
							horaColumna = t;
							closeCTime =  aux2; 
						}
					}
				}

			} //Fin bucle for

			try {
				Statement st = con.createStatement();
				st.executeUpdate("UPDATE User SET closeC = '1' WHERE id = '"+id+"'");
				st.executeUpdate("UPDATE User SET closeCTime = '"+closeCTime+"' WHERE id = '"+id+"'");
				st.close();
			}catch(SQLException e) {
				e.printStackTrace();
			}

			System.out.println("Añadimos contacto estrecho a la BBDD, en fecha y hora: "+ closeCTime);

			return "Contacto estrecho";

		}
		return rslt;



	}

	private synchronized String obtenerPosiciones2h(String id,String lat,String lon,String time, String lat22,String lon22,String time22) throws ParseException {
		String rslt= "";
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM Location WHERE idUser IN (SELECT id FROM User WHERE status == 1) AND idUser IS NOT '"+id+"'");
			//Obtenemos todas las posiciones de los usuarios que est�n infectados 
			//Obtener las posiciones que est�n a menos de 2 m y como m�ximo 48 horas
			while(rs.next()) {
				int x2 = rs.getInt(1);
				String s2 = rs.getString(2);
				String lat2 = rs.getString(3);
				String lon2 = rs.getString(4);
				String time2 = rs.getString(5);
				double diff = 999999;
				try {
					diff = dateFormat.parse(time).getTime() - dateFormat.parse(time2).getTime();
				}catch(ParseException pe) {
					pe.printStackTrace();
				}
				double dis = Math.sqrt(Math.pow((Double.valueOf(lat2) - Double.valueOf(lat)),2) + Math.pow((Double.valueOf(lon2) - Double.valueOf(lon)),2));
				if(diff/1000/60 <= 2880 && diff/1000/60 >= 0 && dis <=2) {
					rslt += x2+" "+s2+" "+lat2+" "+lon2+" "+time2+"         Datos Usuario Posible Contacto Estrecho ---> "+lat22+" "+lon22+" "+time22+"\n";
				}
			}

		}catch(SQLException e) {
			e.printStackTrace();
		}
		return rslt;
	}

	private synchronized String cuarentena(String id, boolean login,DataInputStream inStream,DataOutputStream outStream,String clientMessage,String[] command) {
		String rslt="";
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		String closeCTime = "";
		double diff = 0;
		String email="";
		if(login) {
			Calendar calendario = Calendar.getInstance();
			String timeLog = calendario.get(Calendar.DAY_OF_MONTH) + "-" + (calendario.get(Calendar.MONTH)+1) + "-" + calendario.get(Calendar.YEAR) + " " + calendario.get(Calendar.HOUR_OF_DAY) + ":" + calendario.get(Calendar.MINUTE) + ":" + calendario.get(Calendar.SECOND);
			try{
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM User WHERE id = '"+id+"' AND closeC = '"+1+"'");//AND Comprobamos si es contacto estrecho
				while(rs.next()) {
					email = rs.getString(2);
					closeCTime = rs.getString(8);

					rslt +=closeCTime+"\n";
				}
				System.out.println("Fecha en la que se ha producido el posible contacto: "+rslt);
				if(rslt.equals(""))
					System.out.println("No es un contacto estrecho");
				else {
					try {
						diff = dateFormat.parse(timeLog).getTime() - dateFormat.parse(closeCTime).getTime();

					} catch (ParseException e) {
						e.printStackTrace();
					}
					if(diff/1000/60 >= 20160) {
						stmt.executeUpdate("UPDATE User SET closeC = '0'");//Eliminar contacto estrecho
						stmt.executeUpdate("UPDATE User SET closeCTime = '0'");
						System.out.println("Usuario "+email+" libre de posible Contacto Estrecho, ha cumplido los 15 días de cuarentena.");
					}else {
						System.out.println("Todavía no han pasado los 15 días desde el "+closeCTime);
					}
				}
			}catch(SQLException e) {
				e.printStackTrace();
			}
		}
		return "Fecha de posible contacto estrecho" +rslt;
	}

	private synchronized String nombreUsuario (String id) {
		String rslt = "";
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("Select email FROM User WHERE id = '"+id+"'");
			while(rs.next()) {
				rslt = rs.getString(1);
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return rslt;
	}

	private synchronized int exit(Integer integer) throws IOException {
		try {
			Statement stmt = con.createStatement();
			int success = stmt.executeUpdate("DELETE FROM Sesion WHERE id == '" + integer + "'");
			stmt.close();
			return success;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}

	private synchronized String getPosition(String id, boolean login,DataInputStream inStream,DataOutputStream outStream) {
		String servermessage = "";
		Statement stmt;
		try {
			stmt = con.createStatement();		
			ResultSet rs = stmt.executeQuery("SELECT * FROM Location WHERE idUser == '" + id + "' ORDER BY Id DESC LIMIT 1");
			String lat = rs.getString(3);
			String lon = rs.getString(4);
			rs.close();
			stmt.close();
			servermessage = lat + " " + lon;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return servermessage;
	}

	private synchronized String getRadius(String id, boolean login) {
		String radius = "0";
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String result = alarm(id,login);
		semaphore.release();
		System.out.println(result);
		if(!(result.split(" "))[3].equals("alarmas")) {
			radius = "100";
		}
		return radius;
	}

	private String getSesions(boolean login) {
		String servermessage = "0";
		if(login) {
			Statement stmt;
			try {
				stmt = con.createStatement();		
				ResultSet rs = stmt.executeQuery("SELECT MAX(id) FROM Sesion");
				servermessage = rs.getString(1);
				rs.close();
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return servermessage;
	}
	
	private String getStatus(boolean login, String id) {
		String servermessage = "0";
		if(login) {
			Statement stmt;
			try {
				stmt = con.createStatement();		
				ResultSet rs = stmt.executeQuery("SELECT status FROM User WHERE id == '" + id + "'");
				servermessage = rs.getString(1);
				rs.close();
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return servermessage;
	}
}



