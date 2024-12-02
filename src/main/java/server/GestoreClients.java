package server;

import java.net.Socket;

public class GestoreClients {
    int i = 0;
    Socket[] All = new Socket[1000];
    String[] Nome = new String[1000];

    synchronized int inc(Socket NuovoSocket) {
        i++;
        int j = 0;
        while (All[j] != null) {
            j++;
        }
        All[j] = NuovoSocket;
        return j;
    }

    synchronized void impostaNome(String n, int dove) {
        Nome[dove] = n;
        System.out.println(Nome[dove] + " " + All[dove].getInetAddress());
    }

    synchronized void dec(int index) {

        Nome[index] = null;
        All[index] = null;
    }
}
