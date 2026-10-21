import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * REST API Client connecting the Java GUI to the Python AI Risk Backend.
 * Uses standard Java 11+ java.net.http.HttpClient with zero external dependencies.
 */
public class ApiClient {
    private final String baseUrl;
    private final HttpClient client;

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(4))
                .build();
    }

    public String checkStatus() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/status"))
                    .timeout(Duration.ofSeconds(4))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            return null;
        }
    }

    public String getAccounts() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/accounts"))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public String evaluateRisk(String senderId, String recipientId, double amount) throws IOException, InterruptedException {
        String jsonPayload = String.format(
                "{\"sender_id\": \"%s\", \"recipient_id\": \"%s\", \"amount\": %.2f}",
                escape(senderId), escape(recipientId), amount
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/evaluate-risk"))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public String executeTransaction(String senderId, String recipientId, double amount, boolean overrideAuthorized) throws IOException, InterruptedException {
        String jsonPayload = String.format(
                "{\"sender_id\": \"%s\", \"recipient_id\": \"%s\", \"amount\": %.2f, \"override_authorized\": %b}",
                escape(senderId), escape(recipientId), amount, overrideAuthorized
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/execute"))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public String getHistory() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/history"))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"");
    }
}
