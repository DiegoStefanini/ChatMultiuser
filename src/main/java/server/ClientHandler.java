package server;
import data.Packet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.Gson;

class ClientHandler implements Runnable {
    //attributi
    Socket link;
    GestoreClients gestore;
    int MioIndice;
    Connection connessione;
    public ClientHandler(Socket s, GestoreClients v, int z, Connection connessione) {
        this.link = s;
        this.gestore = v;
        this.MioIndice = z;
        this.connessione = connessione;
    }

    public void cleanup(int index) {
        try {
            link.close();
        } catch (IOException e) {
        }
        gestore.dec(index);
    }

    public static void attendi(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException _) {
        }
    }
    private static boolean checkCredenziali(Connection connection, String username, String password) {
        String query = "SELECT Nome FROM utenti WHERE Password = ? AND Nome = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, password);
            preparedStatement.setString(2, username);

            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();  // Se c'è un risultato, le credenziali sono corrette
        } catch (SQLException e) {
            System.out.println("Errore durante il login!");
            e.printStackTrace();
            return false;
        }
    }
    private static boolean userExist(Connection connection, String username) {
        String query = "SELECT COUNT(*) FROM utenti WHERE Nome = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0;  // If count > 0, the user exists
            }
        } catch (SQLException e) {
            System.out.println("Errore durante il check se l'utente esiste!");
            e.printStackTrace();
        }
        return false;  // Return false if the user does not exist or in case of an error
    }

    private static boolean addUser(Connection connection, String username, String password) {
        if (userExist(connection, username)) {
            return false;
        }
        String query = "INSERT INTO utenti (Nome, Password) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);  // Primo parametro: username
            preparedStatement.setString(2, password);  // Secondo parametro: password

            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Utente aggiunto con successo!");
                return true;
            } else {
                System.out.println("Nessuna riga inserita.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Errore durante l'inserimento dell'utente!");
            e.printStackTrace();
            return false;
        }
    }

    public void run() {
        BufferedReader RiceviDalClient;
        PrintWriter InviaAlClient;
        try {

            RiceviDalClient = new BufferedReader(new InputStreamReader(link.getInputStream())); // RICEVE MESSAGGI DAL CLIENT
            InviaAlClient = new PrintWriter(link.getOutputStream(), true); // INVIA MESSAGGI AL CLIENT
            Gson gson = new Gson(); // Crea istanza di gson
            Packet PacketRicevuto = null;
            while (PacketRicevuto == null || !"LOGOUT".equals(PacketRicevuto.getHeader())) {//  da cambiare questa condizione, non va bene, troviamo un altro modo
                String json = RiceviDalClient.readLine(); // Richiesta bloccante

                PacketRicevuto = gson.fromJson(json, Packet.class); // converte la stringa che gli è arrivata in Packet
                Packet PacketDaInviare = null;
                if ("REGISTRA".equals(PacketRicevuto.getHeader())) { // se REGISTRA == PacketRicevuto.getHeader();
                    if (!addUser(connessione, PacketRicevuto.getMittente(), PacketRicevuto.getContenuto())) {
                        PacketDaInviare = new Packet("NOTIFICATION", "","", "L'utente che hai scelto esiste già", true);
                    } else {
                        PacketDaInviare = new Packet("NOTIFICATION", "","", "Utente creato con successo", false);
                    }

                } else if ("LOGIN".equals(PacketRicevuto.getHeader())) {
                    String Username = PacketRicevuto.getMittente();
                    if (checkCredenziali(connessione,Username, PacketRicevuto.getContenuto())) {
                        PacketDaInviare = new Packet("NOTIFICATION", Username,"", "Hai effettuato l'accesso !", false);
                    } else {
                        PacketDaInviare = new Packet("NOTIFICATION", "","", "Le credenziali non sono corrette", true);
                    }
                }
                json = gson.toJson(PacketDaInviare);
                InviaAlClient.println(json);
            }
        }catch(IOException ex) {
            ex.printStackTrace();
            System.out.println("Client died too early, cleaning up the mess!");
            cleanup(MioIndice);
            return;
        }
        try {
            RiceviDalClient.close();
            InviaAlClient.close();
        } catch (IOException e) {
        }
        cleanup(MioIndice);

    }
}