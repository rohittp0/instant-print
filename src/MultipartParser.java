import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MultipartParser {
    private final StringBuilder builder = new StringBuilder();
    private final String mime;

    public MultipartParser(InputStream inputStream) throws IOException {
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream)))
        {
            String div = readUntil(reader, "------");
            assert div != null : "Invalid multipart request, no separator found";

            this.mime = readUntil(reader, "Content-Type:").split("Content-Type:")[1].trim();
            assert readUntil(reader, "\n") != null : "Invalid multipart request, no mime found";;



            for(String line = reader.readLine(); line != null; line = reader.readLine()) {
                if(line.equals(div)) {
                    break;
                }

                builder.append(line);
            }
        }
    }

    private String readUntil(@NotNull BufferedReader reader, String until) throws IOException {
        String line = reader.readLine();

        while (line != null && !line.startsWith(until))
            line = reader.readLine();

        return line;
    }

    public String getMime() {
        return mime;
    }

    public String getFileContent() {
        return builder.toString();
    }
}
