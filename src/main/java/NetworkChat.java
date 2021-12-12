import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class NetworkChat extends JFrame {
    private JTextField message;
    private JTextArea chatArea;
    private JTextArea clientsList;
    private JButton sendButton;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nick = "";
    private boolean timeOutStatus=false;

    public NetworkChat() throws IOException {
        setTitle("Сетевой чат");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(300,300,400,400);
        JLabel header = new JLabel("Сетевой чат");
        header.setFont(new Font("Arial",Font.BOLD,16));
        add(header,BorderLayout.PAGE_START);
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        add(chatArea,BorderLayout.CENTER);
        clientsList = new JTextArea();
        clientsList.setEditable(false);
        add(clientsList,BorderLayout.LINE_END);
        JPanel downPanel = new JPanel();
        downPanel.setLayout(new BoxLayout(downPanel,BoxLayout.LINE_AXIS));
        message = new JTextField();
        message.addActionListener(e -> {
            sendMessage();
        });
        message.setText("/auth login1 pass1");
        downPanel.add(message);
        sendButton = new JButton("Отправить");
        sendButton.addActionListener(e -> {
            sendMessage();
        });
        downPanel.add(sendButton);
        add(downPanel,BorderLayout.PAGE_END);
        setVisible(true);
        openConnection();
        startTread();

    }
    private void closeConnection(){
        if (!timeOutStatus) {
            chatArea.append("# Вы вышли из чата\n");
            message.setText("/auth login1 pass1");
            clientsList.setText("");
        }
        else{
            chatArea.append("Истекло время отведенное на авторизацию.\n");
            chatArea.append("Соединение разорвано\n");
            chatArea.append("Перезапустите приложение");
            message.setText("");
            message.setEnabled(false);
            sendButton.setEnabled(false);
        }
        try {
            in.close();
            out.close();
            socket.close();
            if (!timeOutStatus){
                openConnection();
                startTread();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void openConnection() throws IOException {
        socket = new Socket("localhost",8189);
        in = new DataInputStream((socket.getInputStream()));
        out = new DataOutputStream((socket.getOutputStream()));
        socket.setSoTimeout(120000);
        timeOutStatus = false;
    }

    public void startTread(){
        new Thread(()->{
            try{
                while (true && !timeOutStatus){
                    String strFromServer = in.readUTF();
                    if (strFromServer.startsWith("/authok")){
                        nick = strFromServer.split(" ")[1];
                        chatArea.append("# Вы авторизованы как: " +nick + "\n");
                        socket.setSoTimeout(0);
                        break;
                    }
                    chatArea.append(strFromServer + "\n");
                }
                while (true && !timeOutStatus){
                    String strFromServer = in.readUTF();
                    if (strFromServer.equalsIgnoreCase("/end")){
                        break;
                    }
                    if (strFromServer.startsWith("/clients")){
                        String clients = strFromServer.substring(9);
                        clientsList.setText(clients.replace(' ','\n'));
                    }
                    else {
                        chatArea.append(strFromServer + "\n");
                    }
                }
            } catch (SocketTimeoutException e){
                System.out.println("Time out connection");
                timeOutStatus = true;
            } catch (Exception e){
                e.printStackTrace();
            } finally {
             closeConnection();
            }
        }).start();
    }

    public void sendMessage(){
        String trimmedMessage = this.message.getText().trim();
        if (!trimmedMessage.isEmpty()){
            message.setText("");
            try{
                if (socket == null || socket.isClosed() && !timeOutStatus){
                    openConnection();
                    startTread();
                }
                out.writeUTF(trimmedMessage);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) throws IOException {
        new NetworkChat();
    }
}

