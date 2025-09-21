package fun.icystal.chat.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.memory.jdbc.MysqlChatMemoryRepository;
import com.alibaba.cloud.ai.memory.redis.RedissonRedisChatMemoryRepository;
import fun.icystal.chat.advisor.RecordAdvisor;
import fun.icystal.chat.prompt.SystemPromptBuilder;
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

    private static final int MAX_MESSAGES = 5;

    private final ChatClient chatClient;

    private final MessageWindowChatMemory messageWindowChatMemory;

    private final SystemPromptBuilder systemPromptBuilder;

    public ChatController(ChatClient.Builder builder, RedissonRedisChatMemoryRepository redisChatMemoryRepository, SystemPromptBuilder systemPromptBuilder, RecordAdvisor recordAdvisor) {
        this.messageWindowChatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(redisChatMemoryRepository)
                .maxMessages(MAX_MESSAGES)
                .build();

        this.chatClient = builder
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(messageWindowChatMemory)
                                .build(),
                        recordAdvisor
                )
                .build();

        this.systemPromptBuilder = systemPromptBuilder;
    }

    @PostMapping("/call")
    public MeiResponse<?> call(@RequestBody CallRequest request) {
        String content = chatClient
                .prompt()
                .system(systemPromptBuilder.systemPrompt())
                .user(request.getQuery())
                .advisors(
                        a -> a.param(CONVERSATION_ID, Objects.requireNonNull(UserHolder.getConversationId()))
                )
                .options(DashScopeChatOptions.builder()
                        .withTemperature(0.55)
                        .build())
                .call()
                .content();
        return MeiResponse.success(content);
    }

    @GetMapping("/history")
    public List<Message> history() {
        String conversationId = UserHolder.getConversationId();
        assert conversationId != null;
        return messageWindowChatMemory.get(conversationId);
    }

}
