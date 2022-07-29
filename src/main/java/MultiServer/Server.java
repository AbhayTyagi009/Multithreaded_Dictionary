package MultiServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray; 
import org.json.simple.JSONObject; 
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Server {
    int port;
    ServerSocket server = null;
    Socket client = null;
    ExecutorService pool = null;
    int clientCount = 0;
    int PORT;
    String PATH;
    
    Server() {
        PORT = 1234;
        PATH = "D:\\Projects\\College\\Distributed\\lib\\dict.json";
    }
    
    Server(int port) {
        this.PORT = port;
        pool = Executors.newFixedThreadPool(10);
    }
    
    public void startServer() throws IOException {
        server = new ServerSocket(PORT);
        System.out.println("Server Started...");
        while(true) {
            client = server.accept();
            clientCount++;
            ServerThread runnable=new ServerThread(client, clientCount, this);
            pool.execute(runnable);   
        }
    }
    
    public static void main(String[] args) throws IOException {
        Server server = new Server();
        if (args.length == 2) {
            server.PORT = Integer.parseInt(args[0]);
            server.PATH = args[1];
        }
        Server serverObj = new Server(server.PORT);
        serverObj.startServer();
    }
    
    private static class ServerThread implements Runnable {
        Server server = null;
        Socket client = null;
        BufferedReader br, b;
        PrintWriter pw;
        JSONObject jo, jo1;
        JSONArray ja, ja1;
        Object obj;
        int id, FLAG;
        String output, FILE_PATH;
        Server s;
        
        ServerThread(Socket client, int count, Server server)throws IOException {
            this.client = client;
            this.server = server;
            this.id = count;
            s = new Server();
            FILE_PATH = s.PATH;
            b = new BufferedReader(new InputStreamReader(client.getInputStream()));
            br = new BufferedReader(new InputStreamReader(System.in));
            pw = new PrintWriter(client.getOutputStream());
            jo = new JSONObject();
            ja = new JSONArray();
            System.out.println("Connection "+id+" established with client "+client);
        }
        
        String parseInput(String query) throws IOException {
            String[] temp = query.split("::::");
            int operation = Integer.parseInt(temp[0]);
            switch (operation) {
                case 1:
                    output = search(temp[1]);
                    break;
                case 2:
                    output = add(temp[1], temp[2]);
                    break;
                case 3:
                    output = delete(temp[1]);
                    break;
                default:
                    break;
            }
            return output;
        }
        
        String search(String word) {
            String mean = (String)jo1.get(word);
            if(mean==null){
                return "-0";
            }
            else
                return mean;
        }
        
        String add(String word, String meaning) throws IOException {
            String mean = search(word);
            boolean a;
            if(mean.equals("-0")) {
                jo1.put(word, meaning);
                a = writeToFile();
                if(a)
                    return "+1";
                else
                    return "-11";
            }
            else return "-11";
        }
        
        String delete (String word) throws IOException {
            String mean = search(word);
            boolean a;
            if(!mean.equals("-0")) {
                jo1.remove(word);
                a = writeToFile();
                if(a)
                    return "+2";
                else
                    return "-22";
            }
            else return "-22";
        }
        
        boolean writeToFile() throws IOException {
            try (FileWriter fileWriter = new FileWriter(FILE_PATH)) {
                fileWriter.write(jo1.toJSONString());
            }
            System.out.println("JSON Object Successfully"
                    + " written to the file!!");
            return true;
        }
        
        @Override
        public void run() {
            String a;
            try {
                obj = new JSONParser().parse(new FileReader(FILE_PATH));
                jo1 = (JSONObject)obj;
                a = b.readLine();
                System.out.println("Reading Data :: "+a);
                String meaning = parseInput(a);
                switch (meaning) {
                    case "-0":
                        pw.write("\nWord not found!! Please add the word with "
                                + "a meaning, of search for another word!\n");
                        break;
                    case "+1":
                        pw.write("\nAdd succesful!!\n");
                        break;
                    case "-11":
                        pw.write("\nAdd failed!!\n");
                        break;
                    case "+2":
                        pw.write("\nDelete succesful!!\n");
                        break;
                    case "-22":
                        pw.write("\nWord doesn't exist in database. Delete "
                                + "failed!!\n");
                        break;
                    default:
                        pw.write("\n"+meaning+"\n");
                        break;
                }
                pw.flush();
            }
            
            catch(Exception e) {
                System.out.println("Something went wrong...! "+e);
            }
            
            finally {
                try {
                    br.close();
                } 
            
                catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
                pw.close();
        }
        }
    }
}