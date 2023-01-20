import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.print.PrinterException;
import java.io.*;
import java.net.InetSocketAddress;


import static java.net.HttpURLConnection.HTTP_OK;

public class Main {
    private final static String HOST = "0.0.0.0";
    private final static int PORT = 8000;
    private final static String GET = "GET";
    private final static String POST = "POST";

    private Main() throws IOException {
        InetSocketAddress socket = new InetSocketAddress(HOST, PORT);
        HttpServer server = HttpServer.create(socket, 0);
        server.createContext("/", this::index);
        server.createContext("/print", this::print);

        server.setExecutor(null); // creates a default executor
        server.start();

        System.out.println("Server listening at : " + HOST + ":" + PORT);
    }

    @Contract("_, _ -> param1")
    private @NotNull HttpExchange writeHTML(@NotNull HttpExchange exchange, @NotNull String text) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html");
        exchange.sendResponseHeaders(HTTP_OK, text.getBytes().length);

        OutputStream output = exchange.getResponseBody();

        output.write(text.getBytes());
        output.flush();

        return exchange;
    }

    private void index(@NotNull HttpExchange exchange) throws IOException {
        if (!GET.equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }


        // Read file index.html
        File file = new File("templates/index.html");
        String html = new String(java.nio.file.Files.readAllBytes(file.toPath()));

        writeHTML(exchange, html).close();
    }

    private void print(@NotNull HttpExchange exchange) throws IOException {
        if (!POST.equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        File output;

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody())))
        {
            String line = reader.readLine();
            String[] parts = line.split("base64,");

            if (parts.length != 2) {
                writeHTML(exchange, "Invalid base64").close();
                return;
            }

            String mime = parts[0].split(":")[1].split(";")[0];

            if (!mime.equals("application/pdf")) {
                writeHTML(exchange, "Invalid mime type").close();
                return;
            }

            // Write bytes to file output.pdf
            output = new File("uploads/"+Math.random()+".pdf");
            byte[] bytes = java.util.Base64.getDecoder().decode(parts[1]);
            java.nio.file.Files.write(output.toPath(), bytes);
        }

        PrintPDF printPDF = new PrintPDF(output);

        try {
            printPDF.print();
        } catch (PrinterException e) {
            throw new RuntimeException(e);
        }


        writeHTML(exchange, "<h1>Printing...</h1>").close();
    }

    public static void main(String[] args) {
        try {
            new Main();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}