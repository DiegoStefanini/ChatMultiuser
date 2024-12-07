package client;

import data.Packet;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import com.google.gson.Gson;

public class AttendiRichieste implements Runnable {
    private static BufferedReader RiceviDalServer;
    private static Gson gson = new Gson();
    private static String inChat;
    private static ArrayList<Integer> NuoviMessaggi;
    public AttendiRichieste(BufferedReader ricevi) {
        RiceviDalServer = ricevi;
    }
    public static void cleenup() {
        for (int i = 0; i != 50; i++) {
            System.out.println();
        }
    }
    public static String[] getMenu(String newMessage) {
        try {
            String json = RiceviDalServer.readLine();
            Packet PacketRicevuto = gson.fromJson(json, Packet.class);
            System.out.println(PacketRicevuto.getContenuto());
            String[] Chats = PacketRicevuto.getContenuto().split(",\\s*");
            if (Chats.length > 0) {
                for (int j = 2; j != Chats.length + 2; j++) {
                    if (newMessage.equals(Chats[j-2]))  {
                        NuoviMessaggi.add(j - 2, NuoviMessaggi.get(j - 2) + 1);
                    }
                    System.out.println(j + " - " + Chats[j - 2] + "[" + NuoviMessaggi.get(j - 2) + "]");
                }
            }
            System.out.println("0 - Termina il programma");
            System.out.println("1 - Cerca utente o crea gruppo");
            return Chats;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void setChat(String chi) {
        inChat = chi;
    }
    public void run() {

        String json;
        Packet PacketRicevuto;
        while (true) {
            try {
                json = RiceviDalServer.readLine();
                PacketRicevuto = gson.fromJson(json, Packet.class);
                if ("MESSAGE".equals(PacketRicevuto.getHeader())) {
                    if (inChat.equals(PacketRicevuto.getDestinatario())) {
                        System.out.println(PacketRicevuto.getMittente() + ": " + PacketRicevuto.getContenuto());
                    } else if (inChat.equals("")) {
                        cleenup();
                        getMenu(PacketRicevuto.getMittente());
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
