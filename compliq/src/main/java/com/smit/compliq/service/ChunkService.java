package com.smit.compliq.service;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;

@Service
public class ChunkService {

    public List<Document> chunkDocuments(List<Document> rawDocuments) {
        TokenTextSplitter splitter = new TokenTextSplitter();
        return splitter.apply(rawDocuments);
    }
}
