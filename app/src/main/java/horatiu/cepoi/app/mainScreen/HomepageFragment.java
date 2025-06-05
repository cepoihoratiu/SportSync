package horatiu.cepoi.app.mainScreen;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import horatiu.cepoi.app.R;
import horatiu.cepoi.app.data.ArticleAdapter;
import horatiu.cepoi.app.data.models.Article;
import horatiu.cepoi.app.repositories.ArticleRepository;
import horatiu.cepoi.app.repositories.UserRepository;

public class HomepageFragment extends Fragment {

    private static final String ADMIN_ID = "3VDP7Aoj2cT5DNHSu4t6";
    private String userId;
    private RecyclerView recyclerView;
    private ArticleAdapter adapter;
    private final List<Article> articleList = new ArrayList<>();
    private final ArticleRepository articleRepository = new ArticleRepository();
    private final UserRepository userRepository = new UserRepository();

    private EditText inputTitle, inputContent, inputImageUrl;
    private Button postArticleButton;

    public HomepageFragment() {}

    public static HomepageFragment newInstance(String userId) {
        HomepageFragment fragment = new HomepageFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            Log.d("Homepage", "Logged in user ID: " + userId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_homepage, container, false);

        recyclerView = view.findViewById(R.id.articlesRecyclerView);
        inputTitle = view.findViewById(R.id.inputTitle);
        inputContent = view.findViewById(R.id.inputContent);
        inputImageUrl = view.findViewById(R.id.inputImageUrl);
        postArticleButton = view.findViewById(R.id.postArticleButton);

        postArticleButton.setOnClickListener(v -> postArticle());

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ArticleAdapter(articleList, userId,
                position -> {
                    Article article = articleList.get(position);
                    articleRepository.updateArticleApproval(article.id, !article.approved, new ArticleRepository.ArticleCallback() {
                        @Override
                        public void onSuccess(Object result) {
                            article.approved = !article.approved;
                            adapter.notifyItemChanged(position);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getContext(), "Error updating approval", Toast.LENGTH_SHORT).show();
                        }
                    });
                },
                position -> {
                    Article article = articleList.get(position);
                    articleRepository.deleteArticle(article.id, new ArticleRepository.ArticleCallback() {
                        @Override
                        public void onSuccess(Object result) {
                            articleList.remove(position);
                            adapter.notifyItemRemoved(position);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getContext(), "Error deleting article", Toast.LENGTH_SHORT).show();
                        }
                    });
                });

        recyclerView.setAdapter(adapter);

        loadArticles();

        return view;
    }

    private void postArticle() {
        String title = inputTitle.getText().toString().trim();
        String content = inputContent.getText().toString().trim();
        String imageUrl = inputImageUrl.getText().toString().trim();

        if (title.isEmpty() || imageUrl.isEmpty() || content.isEmpty()) {
            Toast.makeText(getContext(), "Provide title, content and imageUrl", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.WEB_URL.matcher(imageUrl).matches() ||
                !imageUrl.matches(".*\\.(jpg|jpeg|png|gif|webp)$")) {
            Toast.makeText(getContext(), "Invalid image URL. Must be a valid URL\" (jpg/jpeg/png/webp)", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.WEB_URL.matcher(content).matches()) {
            Toast.makeText(getContext(), "Invalid content URL", Toast.LENGTH_SHORT).show();
            return;
        }
        // The userId passed to newInstance is the author's ID
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(getContext(), "User not identified. Cannot post article.", Toast.LENGTH_LONG).show();
            Log.e("Homepage", "Author ID is null or empty in postArticle");
            return;
        }

        userRepository.getUserById(userId, new UserRepository.UserCallback() { // userId here is the author's ID
            @Override
            public void onSuccess(Object result) {
                Map<String, Object> userData = (Map<String, Object>) result;
                String authorName = userData.containsKey("name") && userData.get("name") != null ? String.valueOf(userData.get("name")) : "Anonymous";

                // Create Article object WITH authorId
                Article article = new Article(null, title, content, imageUrl, authorName, userId, false); // Pass userId as authorId

                articleRepository.addArticle(article, new ArticleRepository.ArticleCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        Toast.makeText(getContext(), "Published article", Toast.LENGTH_LONG).show();
                        inputTitle.setText("");
                        inputContent.setText("");
                        inputImageUrl.setText("");
                        loadArticles();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Error at posting article " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error at getting user" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadArticles() {
        if (ADMIN_ID.equals(userId)) {
            articleRepository.getAllArticles(new ArticleRepository.ArticleListCallback() {
                @Override
                public void onSuccess(List<Article> result) {
                    articleList.clear();
                    articleList.addAll(result);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("Homepage", "Error loading all articles", e);
                }
            });
        } else {
            articleRepository.getAllApprovedArticles(new ArticleRepository.ArticleListCallback() {
                @Override
                public void onSuccess(List<Article> result) {
                    articleList.clear();
                    articleList.addAll(result);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("Homepage", "Error loading approved articles", e);
                }
            });
        }
    }

}
