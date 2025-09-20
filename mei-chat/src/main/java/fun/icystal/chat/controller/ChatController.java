package fun.icystal.chat.controller;

import com.alibaba.cloud.ai.memory.redis.RedissonRedisChatMemoryRepository;
import fun.icystal.chat.wrapper.CallRequest;
import fun.icystal.chat.wrapper.MeiResponse;
import fun.icystal.core.context.UserHolder;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final int MAX_MESSAGES = 100;

    private final ChatClient chatClient;

    private final MessageWindowChatMemory messageWindowChatMemory;

    public ChatController(ChatClient.Builder builder, RedissonRedisChatMemoryRepository redisChatMemoryRepository) {
        this.messageWindowChatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(redisChatMemoryRepository)
                .maxMessages(MAX_MESSAGES)
                .build();

        this.chatClient = builder
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(messageWindowChatMemory)
                                .build()
                )
                .build();
    }

    @PostMapping("/call")
    public MeiResponse<?> call(@RequestBody CallRequest request) {
        String content = chatClient.prompt(request.getQuery())
                .advisors(
                        a -> a.param(CONVERSATION_ID, Objects.requireNonNull(UserHolder.getConversationId()))
                )
                .call()
                .content();
        return MeiResponse.success(content);
    }

    @GetMapping("/history/{conversation_id}")
    public List<Message> history(@PathVariable("conversation_id") String conversationId) {
        return messageWindowChatMemory.get(conversationId);
    }

}
