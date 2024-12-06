package client;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Scanner;
import com.google.gson.Gson;
import data.Packet;
public class ClientMain {

    private static final int MAX_TRY = 3;
    private static String NomeClient = null;
    private static ArrayList<String> Chats;
    public static void attendi(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException _) {
        }
    }

    public static void main(String[] args) {
        // Configura l'indirizzo IP e la porta del server
        String serverAddress = "127.0.0.1"; // IP del server (localhost)
        int port = 12345; // Porta del server
        Socket link = null;
        Gson gson = new Gson(); // String json = gson.toJson(packet); Packet deserializedPacket = gson.fromJson(json, Packet.class)

        int i = 0;
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
            boolean avanti = false;
            int scelta;
            while (!avanti) {
                System.out.println("1 - Login");
                System.out.println("2 - Register");
                System.out.println("0 - Exit");
                Packet PacketRicevuto = null;
                scelta = scanner.nextInt();
                scanner.nextLine(); // pulisco buffer (non so perche ma sennò non funziona)
                int tentativi = 0;
                String reins = "";
                switch (scelta) {
                    case 1:
                        // TROVIAMO UN MODO DI FARE CASE 1 E 2 IN UNA FUNZIONE, DATO CHE CAMBIA POCHISSIME COSE
                        System.out.println("Hai scelto Login.");
                        while (PacketRicevuto == null || PacketRicevuto.getError()) {
                            if (tentativi > 0) {
                                reins = "di nuovo";
                            }
                            // DA AGGIUNGERE "PREMI 0 PER ANDARE INDIETRO" !!!
                            System.out.println("Inserisci "+reins+" il nome utente: ");
                            String nome = scanner.nextLine();
                            System.out.println("Inserisci "+reins+" la tua password: ");
                            String password = scanner.nextLine();
                            Packet DaInviareAlServer = new Packet("LOGIN", "", nome, password, false);
                            String json = gson.toJson(DaInviareAlServer); // converto il pacchetto in json
                            MandaAlServer.println(json);  // mando al server
                            json = RiceviDalServer.readLine(); // ASPETTO CHE MI RISPONDA COME è ANDATA LA REGISTRAZIONE
                            PacketRicevuto = gson.fromJson(json, Packet.class);
                            System.out.println(PacketRicevuto.getContenuto());
                        //    attendi(2000);
                            tentativi++;
                        }
                        NomeClient = PacketRicevuto.getDestinatario();
                        avanti = true;
                        break;
                    case 2:
                        while (PacketRicevuto == null || PacketRicevuto.getError()) {
                            if (tentativi > 0) {
                                reins = "di nuovo";
                            }
                            // DA AGGIUNGERE "PREMI 0 PER ANDARE INDIETRO" !!!
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
                        //    attendi(2000);
                            tentativi++;
                        }
                        NomeClient = PacketRicevuto.getDestinatario();
                        avanti = true;
                        break;

                    case 0:
                        System.out.println("Uscita dal programma.");
                        break;

                    default:
                        System.out.println("Scelta non valida. Inserisci 1, 2 o 0.");
                        break;
                }

                // richiesta chat dal database
                Packet DaInviareAlServer = new Packet("CHAT", "", "", "", false);
                String json = gson.toJson(DaInviareAlServer); // converto il pacchetto in json
                MandaAlServer.println(json);  // mando al server

                json = RiceviDalServer.readLine(); // ASPETTO CHE MI RISPONDA COME è ANDATA LA REGISTRAZIONE
                PacketRicevuto = gson.fromJson(json, Packet.class);
                System.out.println(PacketRicevuto.getContenuto());
                String[] Chats = PacketRicevuto.getContenuto().split(",\\s*");
                if (Chats.length > 0) {
                    for (int j = 1; j != Chats.length + 1; j++) {
                        System.out.println(j + " - " + Chats[j - 1] );
                    }
                }
                System.out.println("0 - Cerca utente o crea gruppo");
                scelta = scanner.nextInt();
                scanner.nextLine();
                if (scelta == 0) {
                    String cerca;
                    System.out.println("Cerca l'utente con cui vuoi chattare o premi");
                    System.out.println("1 - Per creare un gruppo");
                    cerca = scanner.nextLine();
                    if ("1".equals(cerca)) {

                    } else {
                        DaInviareAlServer = new Packet("AVVIACHAT", cerca, NomeClient, "", false);
                        json = gson.toJson(DaInviareAlServer); // converto il pacchetto in json
                        MandaAlServer.println(json);
                        json = RiceviDalServer.readLine(); // ASPETTO CHE MI RISPONDA COME è ANDATA LA REGISTRAZIONE
                        PacketRicevuto = gson.fromJson(json, Packet.class);
                        if (PacketRicevuto.getError()) {
                            System.out.println(PacketRicevuto.getContenuto());
                        } else {
                            System.out.println(PacketRicevuto.getContenuto());

                        }
                    }
                }
               // json = RiceviDalServer.readLine();
               // PacketRicevuto = gson.fromJson(json, Packet.class);

            }
            link.close();
        } catch (Exception e) {
            // Gestisce eventuali errori
            System.err.println("Errore: " + e.getMessage());
            e.printStackTrace();
        }
    }

}