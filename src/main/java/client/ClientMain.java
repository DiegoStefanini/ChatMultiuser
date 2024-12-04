package client;

import com.sun.source.tree.BindingPatternTree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import com.google.gson.Gson;
import data.Packet;
public class ClientMain {

    private static final int MAX_TRY = 3;

    public static void attendi(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        }
    }

    public static void main(String[] args) {
        // Configura l'indirizzo IP e la porta del server
        String serverAddress = "127.0.0.1"; // IP del server (localhost)
        int port = 12345; // Porta del server
        String input;
        int scelta;
        int selezione;
        Socket link = null;
        int i = 0;
        Boolean avanti = false;
        Gson gson = new Gson(); // String json = gson.toJson(packet); Packet deserializedPacket = gson.fromJson(json, Packet.class)

        while( link == null && i < MAX_TRY ) {
            try {
                // Crea una connessione al server
                link = new Socket(serverAddress, port);
            }catch(IOException ex) {
                System.out.println("Connessione non ruscita, tentativo "+(i+1)+ "/3");

                attendi((long) (1000*Math.pow(2, i)));
            }
            i++;
        }

        if( link == null ) {
            System.out.println("Impossibile collegarsi al server");
            return;
        }

        try {
            PrintWriter MandaAlServer = new PrintWriter(link.getOutputStream(), true);
            BufferedReader RiceviDalServer = new BufferedReader(new InputStreamReader(link.getInputStream()));

            Scanner scanner = new Scanner(System.in);

            while (!avanti) {
                System.out.println("1 - Login");
                System.out.println("2 - Register");
                System.out.println("0 - Exit");
                Packet PacketRicevuto = null;
                scelta = scanner.nextInt();
                scanner.nextLine(); // pulisco buffer (non so perche ma sennò non funziona)
                switch (scelta) {
                    case 1:
                        System.out.println("Hai scelto Login.");
                        avanti = true;
                        // Implementa la logica del login
                        break;

                    case 2:
                        System.out.println("Hai scelto Register.");
                        int tentativi = 0;
                        String reins = "";
                        while (PacketRicevuto == null || PacketRicevuto.getError()) {
                            if (tentativi > 0) {
                                reins = "di nuovo";
                            }
                            System.out.println("Inserisci "+reins+" il nome utente: ");
                            String nome = scanner.nextLine();
                            System.out.println("Inserisci "+reins+" la tua password: ");
                            String password = scanner.nextLine();
                            // DA AGGIUNGERE CONTROLLO SE NON INSERISCE VUOTO !!!
                            Packet DaInviareAlServer = new Packet("REGISTRA", "", nome, password, false);
                            String json = gson.toJson(DaInviareAlServer); // converto il pacchetto in json
                            MandaAlServer.println(json);  // mando al server
                            json = RiceviDalServer.readLine(); // ASPETTO CHE MI RISPONDA COME è ANDATA LA REGISTRAZIONE
                            PacketRicevuto = gson.fromJson(json, Packet.class);
                            System.out.println(PacketRicevuto.getContenuto());
                            attendi(2000);
                        }
                        avanti = true;
                        // Implementa la logica della registrazione
                        break;

                    case 0:
                        System.out.println("Uscita dal programma.");
                        avanti = true;
                        break;

                    default:
                        System.out.println("Scelta non valida. Inserisci 1, 2 o 0.");
                        break;
                }
            }
        } catch (Exception e) {
            // Gestisce eventuali errori
            System.err.println("Errore: " + e.getMessage());
            e.printStackTrace();
        }
    }

}