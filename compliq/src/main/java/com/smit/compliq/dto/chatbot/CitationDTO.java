package com.smit.compliq.dto.chatbot;

public class CitationDTO {

    private long documentId;
    private String documentName;
    private String chunkPreview;
    private Integer pageNumber;

    public CitationDTO() {}

    public CitationDTO(long documentId, String documentName, String chunkPreview, Integer pageNumber) {
        this.documentId = documentId;
        this.documentName = documentName;
        this.chunkPreview = chunkPreview;
        this.pageNumber = pageNumber;
    }

    public long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(long documentId) {
        this.documentId = documentId;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getChunkPreview() {
        return chunkPreview;
    }

    public void setChunkPreview(String chunkPreview) {
        this.chunkPreview = chunkPreview;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }
}
