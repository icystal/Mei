package fun.icystal.chat.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * 多层记忆的实现
 * 传递 m 轮完整的对话 作为 短期记忆
 * 传递 m~n 轮对话的摘要 作为 摘要记忆
 * 从历史对话的数据库中查询 k 条历史 作为 长期记忆
 */
public final class MultilayerMemoryAdvisor implements BaseChatMemoryAdvisor {

    /**
     * 短期记忆轮次
     */
    private final int shortTerm = 7;

    /**
     * 摘要范围
     */
    private final int summaryScope = 10;

    /**
     * 长期记忆条数
     */
    private final int longTerm = 5;

    private final ChatMemory chatMemory;

    private final String defaultConversationId;

    private final int order;

    private MultilayerMemoryAdvisor(ChatMemory chatMemory, String defaultConversationId, int order) {
        Assert.notNull(chatMemory, "chatMemory cannot be null");
        Assert.hasText(defaultConversationId, "defaultConversationId cannot be null or empty");
        this.chatMemory = chatMemory;
        this.defaultConversationId = defaultConversationId;
        this.order = order;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {

        String conversationId = getConversationId(chatClientRequest.context(), this.defaultConversationId);

        // 1. Retrieve the chat memory for the current conversation.
        List<Message> memoryMessages = this.chatMemory.get(conversationId);

        // 2. Advise the request messages list.
        List<Message> processedMessages = new ArrayList<>(memoryMessages);
        processedMessages.addAll(chatClientRequest.prompt().getInstructions());

        // 3. Create a new request with the advised messages.
        ChatClientRequest processedChatClientRequest = chatClientRequest.mutate()
                .prompt(chatClientRequest.prompt().mutate().messages(processedMessages).build())
                .build();

        // 4. Add the new user message to the conversation memory.
        UserMessage userMessage = processedChatClientRequest.prompt().getUserMessage();
        this.chatMemory.add(conversationId, userMessage);

        return processedChatClientRequest;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return null;
    }

    @Override
    public int getOrder() {
        return order;
    }
}
