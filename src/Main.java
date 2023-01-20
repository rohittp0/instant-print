import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNull;

import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Main extends WebSocketClient {

    public Main(URI serverURI) {
        super(serverURI);
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        send("Hello");
        System.out.println("opened connection");
    }

    @Override
    public void onMessage(String message) {
        File output = new File("uploads/" + Math.random() + ".pdf");
        byte[] bytes = java.util.Base64.getDecoder().decode(message);

        try {
            java.nio.file.Files.write(output.toPath(), bytes);
            new PrintPDF(output).print();
        } catch (PrinterException | IOException e) {
            System.out.println("Error printing file " + e.getMessage());
            send("Error printing file " + e.getMessage());
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        // The close codes are documented in class org.java_websocket.framing.CloseFrame
        System.out.println(
                "Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: "
                        + reason);

        newClient();
    }

    @Override
    public void onError(@NotNull Exception ex) {
        ex.printStackTrace();
        // if the error is fatal then onClose will be called additionally
    }

    private static void newClient() {
        try {
            new Main(new URI("wss://print.rohittp.com/ws")).connect();
        } catch (URISyntaxException e) {
            System.out.println("Error connecting to websocket " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        newClient();
    }

}