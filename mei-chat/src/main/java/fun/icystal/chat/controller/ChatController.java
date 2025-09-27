package fun.icystal.chat.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import fun.icystal.chat.advisor.MultilayerMemoryAdvisor;
import fun.icystal.chat.advisor.RecordAdvisor;
import fun.icystal.chat.mapper.MessageMapper;
import fun.icystal.chat.util.BeanMapper;
import fun.icystal.chat.wrapper.CallRequest;
import fun.icystal.chat.wrapper.MeiResponse;
import fun.icystal.core.context.UserHolder;
import fun.icystal.core.entity.MessageLog;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatClient chatClient;

    private final MessageMapper messageMapper;

    public ChatController(ChatClient.Builder builder, RecordAdvisor recordAdvisor, MultilayerMemoryAdvisor multilayerMemoryAdvisor, MessageMapper messageMapper) {
        this.messageMapper = messageMapper;

        this.chatClient = builder
                .defaultAdvisors(
                        multilayerMemoryAdvisor,
                        recordAdvisor
                )
                .build();
    }

    @PostMapping("/call")
    public MeiResponse<?> call(@RequestBody CallRequest request) {
        String content = chatClient
                .prompt()
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
        List<MessageLog> messageLogs = messageMapper.selectByConversationId(Objects.requireNonNull(UserHolder.getConversationId()), 1000);
        messageLogs.sort(Comparator.comparing(MessageLog::time));
        if (messageLogs.isEmpty()) {
            return List.of();
        }
        return messageLogs.stream().map(BeanMapper::convertMessage).collect(Collectors.toList());
    }

}
