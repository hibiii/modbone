package hibiscvs.modbone.net;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Request {

    private HttpClient client;
    private HttpRequest request;
    private int ratelimit = 300;
    private int attempts = 3;

    public Request(String uRI) {
        this.client = HttpClient.newHttpClient();
        this.request = HttpRequest.newBuilder(URI.create(uRI)).GET().build();
    }

    public Request ratelimit(int delay) {
        this.ratelimit = delay;
        return this;
    }

    public Request attempt(int times) {
        this.attempts = times;
        return this;
    }

    public String get() {
        String out = null;
        while(this.attempts > 0) {
            HttpResponse<String> response = null;
            try {
                response = this.client.send(this.request, HttpResponse.BodyHandlers.ofString());
            }
            catch (IOException | InterruptedException e) {
                continue;
            }
            this.attempts--;
            if(response.statusCode() == 429) {
                int delay = response.headers().firstValue("Retry-After").map(Integer::parseUnsignedInt).orElse(this.ratelimit);
                System.err.println("WARN: 429 on hitting '%s', waiting %ds, %d retries left".formatted(this.request.uri(), delay, this.attempts));
                try {
                    Thread.sleep(delay * 1000 + 200);
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                continue;
            }
            if(response.statusCode() / 100 == 4) {
                throw new IllegalStateException("Incorrect configuration: received status %d from URI '%s'".formatted(response.statusCode(), this.request.uri()));
            }
            if(response.statusCode() / 100 == 5) {
                System.err.println("WARN: %d on hitting '%s', waiting 30s".formatted(response.statusCode(), this.request.uri(), 30));
                this.attempts++;
                try {
                    Thread.sleep(30 * 1000 + 200);
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                continue;
            }
            out = response.body();
            break;
        }
        return out;
    }
}
