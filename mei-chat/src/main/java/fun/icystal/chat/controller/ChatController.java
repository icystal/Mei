package fun.icystal.chat.controller;

import fun.icystal.chat.wrapper.MeiResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @PostMapping("/call")
    public MeiResponse<?> call(@RequestBody String query) {
        String content = chatClient.prompt(query).call().content();
        return MeiResponse.success(content);
    }

}
