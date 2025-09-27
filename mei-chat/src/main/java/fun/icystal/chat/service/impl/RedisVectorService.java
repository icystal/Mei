package fun.icystal.chat.service.impl;

import fun.icystal.chat.service.VectorService;
import fun.icystal.chat.context.UserHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class RedisVectorService implements VectorService {

    private final RedisVectorStore redisVectorStore;
    private final WebClient.Builder builder;

    @Autowired
    public RedisVectorService(@Qualifier("customRedisVectorStore") RedisVectorStore redisVectorStore, WebClient.Builder builder) {
        this.redisVectorStore = redisVectorStore;
        this.builder = builder;
    }

    @Override
    public List<Document> search(String query, int topK) {
        return redisVectorStore.similaritySearch(SearchRequest.builder()
                .query(query)
                .topK(topK)
                .filterExpression(conversationFilter())
                .build());
    }

    @Override
    public void add(List<Document> documents) {
        redisVectorStore.add(documents);
    }

    private String conversationFilter() {
        String filter = null;
        String conversationId = UserHolder.getConversationId();
        if (StringUtils.isNotBlank(conversationId)) {
            filter = "conversationId=='" + conversationId + "'";
        }
        return filter;
    }
}
