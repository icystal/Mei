package fun.icystal.chat.service;

import org.springframework.ai.document.Document;

import java.util.List;

public interface VectorService {

    List<Document> search(String query, int topK);

    void add(List<Document> documents);

}
