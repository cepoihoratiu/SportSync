package horatiu.cepoi.app.repositories;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import horatiu.cepoi.app.data.models.Article;

public class ArticleRepository {

    private static final String TAG = "ArticleRepository";
    private final FirebaseFirestore db;
    private final CollectionReference articleCollection;

    public ArticleRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.articleCollection = db.collection("posts");
    }

    // ✅ Add new article
    public void addArticle(Article article, final ArticleCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", article.title);
        data.put("content", article.content);
        data.put("imageUrl", article.imageUrl);
        data.put("authorName", article.authorName);
        data.put("authorId", article.authorId);
        data.put("approved", article.approved);
        data.put("timestamp", System.currentTimeMillis());

        articleCollection.add(data)
                .addOnSuccessListener(docRef -> callback.onSuccess(docRef.getId()))
                .addOnFailureListener(callback::onFailure);
    }

    // ✅ Delete article by document ID
    public void deleteArticle(String articleId, final ArticleCallback callback) {
        articleCollection.document(articleId)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess("Article deleted"))
                .addOnFailureListener(callback::onFailure);
    }

    // ✅ Get article by ID
    public void getArticleById(String articleId, final ArticleCallback callback) {
        articleCollection.document(articleId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Article article = doc.toObject(Article.class);
                        callback.onSuccess(article);
                    } else {
                        callback.onFailure(new Exception("Article not found"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void getAllArticles(final ArticleListCallback callback) {
        articleCollection.get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Article> articleList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Article article = document.toObject(Article.class);
                        article.id = document.getId();
                        articleList.add(article);
                    }
                    callback.onSuccess(articleList);
                })
                .addOnFailureListener(callback::onFailure);
    }


    public void getAllApprovedArticles(final ArticleListCallback callback) {
        articleCollection.whereEqualTo("approved", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Article> articleList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Article article = document.toObject(Article.class);
                        article.id = document.getId();
                        articleList.add(article);
                    }
                    callback.onSuccess(articleList);
                })
                .addOnFailureListener(callback::onFailure);
    }


    public void updateArticleApproval(String articleId, boolean approved, final ArticleCallback callback) {
        articleCollection.document(articleId)
                .update("approved", approved)
                .addOnSuccessListener(unused -> callback.onSuccess("Updated"))
                .addOnFailureListener(callback::onFailure);
    }

    // Interfaces
    public interface ArticleCallback {
        void onSuccess(Object result);
        void onFailure(Exception e);
    }

    public interface ArticleListCallback {
        void onSuccess(List<Article> result);
        void onFailure(Exception e);
    }
}
