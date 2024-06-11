package chatroom.server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {

    public static ArrayList<ClientHandler> connectedClients = new ArrayList<>();
    public String clientUsername;
    private Socket socket;

    private BufferedReader bufferedIn;
    private BufferedWriter bufferedBroadcast;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            this.bufferedIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedBroadcast = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.clientUsername = bufferedIn.readLine();
            connectedClients.add(this);
            System.out.println("dlugosc"+connectedClients.size());
            System.out.println("new user connected : " + clientUsername);
            broadcastMsg("new user connected:" + clientUsername );

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void broadcastMsg(String msgToBroadcast){
        for(ClientHandler clientHandler:connectedClients){
                try {
                    if(!clientHandler.clientUsername.equals(this.clientUsername)) {
                        clientHandler.bufferedBroadcast.write(msgToBroadcast);
                        clientHandler.bufferedBroadcast.newLine();
                        clientHandler.bufferedBroadcast.flush();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

        }

    }

    public void disconnectClient(){
        connectedClients.remove(this);
        broadcastMsg("User disconnected:" + clientUsername);
    }

    public void closeEveryThing(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) throws IOException {
        disconnectClient();
        if(bufferedReader != null){
            bufferedReader.close();
        }
        if(bufferedWriter != null){
            bufferedWriter.close();
        }
        if (socket != null){
            socket.close();
        }

    }

    @Override
    public void run() {
        String msgFromClient;
        while (!socket.isClosed()){
            try {
                msgFromClient = bufferedIn.readLine();
                System.out.println(msgFromClient);
                broadcastMsg(msgFromClient);
            } catch (IOException e) {
                try {
                    closeEveryThing(socket,bufferedIn,bufferedBroadcast);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

            }
        }
    }
}
