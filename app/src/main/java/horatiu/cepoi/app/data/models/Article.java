package horatiu.cepoi.app.data.models;

public class Article {
    public String id;
    public String title;
    public String content;
    public String imageUrl;
    public String authorName;
    public String authorId;
    public Boolean approved;
    public long timestamp;
    public Article() {}

    public Article(String id, String title, String content, String imageUrl, String authorName, String authorId, Boolean approved) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.authorName = authorName;
        this.authorId = authorId; // <-- SET AUTHOR ID
        this.approved = approved;
        this.timestamp = System.currentTimeMillis(); // Optional
    }

    // Getters and Setters (or make fields public if you prefer, though getters are good practice)
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getImageUrl() { return imageUrl; }
    public String getAuthorName() { return authorName; }
    public String getAuthorId() { return authorId; } // <-- GETTER FOR AUTHOR ID
    public long getTimestamp() { return timestamp; }
}