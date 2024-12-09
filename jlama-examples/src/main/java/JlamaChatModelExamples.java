import com.github.tjake.jlama.safetensors.DType;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.jlama.JlamaChatModel;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JlamaChatModelExamples {

    static class Simple_Prompt {

        static DateTimeFormatter dtf = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .parseLenient()
            .optionalStart()
                .appendPattern("yyyy-MM-dd")
            .optionalEnd()
            .optionalStart()
                .appendPattern("dd/MM/yy")
            .optionalEnd()
            .optionalStart()
                .appendPattern("dd MMM yyyy")
            .optionalEnd()
            .optionalStart()
                .appendPattern("MMM dd, yyyy")
            .optionalEnd()
            .optionalStart()
                .appendPattern("EEEE, MMMM d, yyyy")
            .optionalEnd()
        // Set strict or lenient parsing. ResolverStyle.STRICT is stricter about format and values.
            .toFormatter(Locale.US)
            .withResolverStyle(ResolverStyle.LENIENT);
        static Pattern pattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

        static ChatLanguageModel model = JlamaChatModel.builder()
                .modelName("Qwen/Qwen2.5-1.5B-Instruct-JQ4")
                //.modelName("tjake/Qwen2.5-0.5B-Instruct-JQ4")
                .temperature(0.0f)
                .maxTokens(100)
                .workingQuantizedType(DType.F32)
                .build();


        public static String normalizeDateAi(String date) {

            List<ChatMessage> msgs = List.of(
                    SystemMessage.from("The users input represents a date. You must convert it to the date format 'YYYY-MM-DD'\n" +
                            "Example: December 20, 22 would return: 2022-12-20\n\n"+
                            "You must return the correctly formatted date ONLY!"),
                    UserMessage.from(date));


            String d = model.generate(msgs).content().text().trim();

            Matcher matcher = pattern.matcher(d);

            if (matcher.find()) {
                // matcher.group() returns the substring that matched the pattern
                return matcher.group();
            } else {
                return d;
            }
        }

        public static String normalizeDate(String date) {

            try {
                LocalDate d = LocalDate.parse(date, dtf);
                return d.format(DateTimeFormatter.ISO_DATE);
            } catch (Exception e) {
                return date;
            }
        }

        public static void main(String[] args) {

            String[] examples = {
                    "2024-12-09",
                    "Dec 09, 2024",
                    "09/12/24",
                    "9 December 2024",
                    "Monday, December 9, 2024",
                    "09-Dec-2024",
                    "09.12.2024",
                    "12/9/2024",
                    "2024/12/09",
                    "2024 December 9",
                    "Dec. 9th, 2024",
                    "9th of December 2024",
                    "Mon, 09 Dec 24",
                    "20241209",
                    "December 9th '24",
                    "Dec-2024-09",
                    "09th December, Twenty Twenty-Four",
                    "09.XII.2024",
                    "9/12/24 (DD/MM/YY)",
                    "12.09.24"
            };


            int correct = 0;
            for (String example : examples) {
                String date = normalizeDateAi(example);
                if (date.trim().equals("2024-12-09")) {
                    correct++;
                    System.out.println("Correct " + example + " => " + date);
                }
                else {
                    System.out.println("Incorrect " + example + " => " + date);
                }
            }

            System.out.println("Found " + correct + " of " + examples.length + " correct");
        }
    }
}
