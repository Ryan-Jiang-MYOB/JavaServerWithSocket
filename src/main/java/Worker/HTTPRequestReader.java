package Worker;

import Constants.HTTPRequestStandard;
import CustomException.InvalidRequestException;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class reads request from InputStream
 */
public class HTTPRequestReader {
    private BufferedReader reader;
    private String requestLine;
    private Map<String, String> headers;
    private String body;

    /**
     * The class reads in the request fields upon instantiation,
     * the reading operations are done in sequence, hence they are private.
     * The Parser will then use getters to access the request fields.
     *
     * @param reader InputStream wrapped in a buffered reader from the client socket.
     * @throws InvalidRequestException
     */
    public HTTPRequestReader(BufferedReader reader) throws InvalidRequestException {
        this.reader = reader;
        this.headers = new HashMap<>();
        readRequestLine();
        readRequestHeaders();
        readRequestBody();
    }

    private void readRequestLine() throws InvalidRequestException {
        try {
            requestLine = reader.readLine();
        } catch (IOException | NullPointerException e) {
            throw new InvalidRequestException("Invalid Request Line - Not being able to read request line");
        }
    }

    private void readRequestHeaders() throws InvalidRequestException {
        String line;
        try {
            line = this.reader.readLine();
            while (!line.isEmpty()) {
                String[] headerTuple = line.split(": ", 2);
                if (headerTuple.length == 2) {
                    this.headers.put(headerTuple[0], headerTuple[1]);
                } else {
                    throw new InvalidRequestException("Invalid Request Header - Invalid header property");
                }
                line = this.reader.readLine();
            }
        } catch (IOException | NullPointerException e) {
            throw new InvalidRequestException("Invalid Request Header - Not being able to read request header");
        }
    }

    private void readRequestBody() throws InvalidRequestException {
        StringBuilder stringBuilder = new StringBuilder();
        if (this.headers.containsKey(HTTPRequestStandard.CONTENT_LENGTH)) {
            try {
                int bodyLength = Integer.parseInt(this.headers.get(HTTPRequestStandard.CONTENT_LENGTH));
                char[] charArray = new char[bodyLength];
                reader.read(charArray, 0, bodyLength);
                stringBuilder.append(charArray);

            } catch (NumberFormatException e) {
                throw new InvalidRequestException("Invalid Request Header - No content-length provided");
            } catch (IOException e) {
                throw new InvalidRequestException("Invalid Request Header - Incorrect content-length provided");
            }
            this.body = stringBuilder.toString();
        } else {
            this.body = "";
        }
    }

    public String getRequestLine() {
        return requestLine;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }
}
