import com.github.tjake.jlama.safetensors.DType;
import dev.langchain4j.agent.tool.*;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.jlama.JlamaChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;


public class JlamaAiFunctionCallingExamples {

    static class Payment_Data_From_AiServices {

        static ChatLanguageModel model = JlamaChatModel.builder()
                .modelName("Qwen/Qwen2.5-1.5B-Instruct-JQ4")
                .temperature(0.0f) //Force same output every run
                .build();

        interface Assistant {
            @SystemMessage({
                    "You are a GitHub support agent.",
                    "You MUST use the github star tool to search requested repo for star count."
            })
            String chat(String userMessage);
        }

        public static void main(String[] args) {
            // STEP 1: User specify tools and query
            // User define all the necessary tools to be used in the chat
            // This example uses the GitHub Stars Tool
            Github_Stars_Tool starsTool = Github_Stars_Tool.build();
            // User define the query to be used in the chat

            //Read chat input from terminal
            System.out.println("How can I help?: ");
            String userMessage =  new Scanner(System.in).nextLine();

            // STEP 2: User asks the agent and AiServices call to the functions
            Assistant agent = AiServices.builder(Assistant.class)
                    .chatLanguageModel(model)
                    .tools(starsTool)
                    .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                    .build();

            // STEP 3: User gets the final response from the agent
            String answer = agent.chat(userMessage);
            System.out.println(answer);
        }
    }

    static class Github_Stars_Tool {
        static Github_Stars_Tool build(){
            return new Github_Stars_Tool();
        }

        // Tool to be executed by mistral model to get payment status
        @Tool("Get number of stars for a github project") // function description
        static Integer getStarCount(@P("The github repo name") String repo) throws IOException, InterruptedException {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/repos/" + repo)) // Replace with your API URL
                    .build();

            // Send the request and get the response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String stars = response.body().split("\"stargazers_count\":")[1].split(",")[0];
            return Integer.parseInt(stars);
        }
    }
}
