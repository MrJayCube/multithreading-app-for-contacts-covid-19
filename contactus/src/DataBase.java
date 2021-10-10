import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DataBase {
	public static void main (String [] args) throws SQLException{
		try{
			Connection con = DriverManager.getConnection("jdbc:sqlite:src/contactus.db");
			// Creando una base de datos
			Statement stmt = con.createStatement();
			stmt.executeUpdate("CREATE TABLE User(id INTEGER PRIMARY KEY , email TEXT, password TEXT, type INT, status boolean,statusTime TEXT,closeC boolean, closeCTime TEXT)");
			stmt.executeUpdate("CREATE TABLE Location(id INTEGER PRIMARY KEY AUTOINCREMENT, idUser INT, lat TEXT, long TEXT, time TEXT, FOREIGN KEY (idUser) REFERENCES User(id))");
			stmt.executeUpdate("CREATE TABLE Sesion(id INTEGER PRIMARY KEY, idUser INT, logued boolean, record boolean, FOREIGN KEY (idUser) REFERENCES User(id))");
			stmt.executeUpdate("CREATE TABLE Log(id INTEGER PRIMARY KEY AUTOINCREMENT, idUser INT, time TEXT, FOREIGN KEY (idUser) REFERENCES User(id))");
			int count = stmt.executeUpdate("INSERT INTO User (email,password,type) VALUES ('pepe','paswd','0')");
			//int count2 = stmt.executeUpdate("INSERT INTO Location (idUser, lat, long, time) VALUES (1,'40','60','30')");
			int count3 = stmt.executeUpdate("INSERT INTO User (email,password,type) VALUES ('admin','admin','1')");
			System.out.println("success "+ count);
			//System.out.println("success "+ count2);
			System.out.println("success "+ count3);

			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM User");
			while (rs.next()) {
				int x = rs.getInt(1);
				String s = rs.getString(2);
				String p = rs.getString(3);
				System.out.println(
						"id: " + x 
						+ "; Usuario: " + s 
						+ "; Contraseña: " + p
				);
			}

			rs = stmt.executeQuery("SELECT * FROM Location");
			while (rs.next()) {
				int x = rs.getInt(1);
				String s = rs.getString(2);
				String lat = rs.getString(3);
				String lon = rs.getString(4);
				String time = rs.getString(5);
				System.out.println(
						"id: " + x 
						+ "; id_Usuario: " + s
						+  "; Latitud: " + lat
						+ "; Longitud: " + lon
						+ "; Marca_Time: " + time
				);
			}
			con.close();

		}catch (SQLException e){
			
			e.printStackTrace();	
		}
	}
	
}
