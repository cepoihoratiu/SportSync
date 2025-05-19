package horatiu.cepoi.app.data;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import horatiu.cepoi.app.R;
import horatiu.cepoi.app.data.models.Article;
import horatiu.cepoi.app.repositories.ArticleRepository;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder> {
    private final List<Article> articleList;
    private final String currentUserId;
    private final ArticleRepository articleRepository = new ArticleRepository();
    private final ArticleAdapter.OnApproveClickListener approveClickListener;
    private final ArticleAdapter.OnDeleteClickListener deleteClickListener;

    public ArticleAdapter(List<Article> articles, String currentUserId,
                          OnApproveClickListener approveClickListener,
                          OnDeleteClickListener deleteClickListener) {
        this.articleList = articles;
        this.currentUserId = currentUserId;
        this.approveClickListener = approveClickListener;
        this.deleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_article, parent, false);
        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        Article article = articleList.get(position);

        holder.title.setText(article.title);
        holder.content.setText(article.content);
        holder.author.setText("By: " + article.authorName);
        Glide.with(holder.itemView.getContext()).load(article.imageUrl).into(holder.image);

        // Open link
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(article.content));
            v.getContext().startActivity(intent);
        });

        // Admin only buttons
        if (currentUserId.equals("tE6ixjCwOybj2IBD9XfE")) {
            holder.approveBtn.setVisibility(View.VISIBLE);
            holder.deleteBtn.setVisibility(View.VISIBLE);

            holder.approveBtn.setText(article.approved ? "✅ Approved" : "❌ Approve");
            holder.approveBtn.setBackgroundColor(article.approved ? Color.GREEN : Color.RED);
            holder.approveBtn.setOnClickListener(v -> approveClickListener.onApproveClick(position));

            holder.deleteBtn.setText("Delete");
            holder.deleteBtn.setOnClickListener(v -> deleteClickListener.onDeleteClick(position));
        } else {
            holder.approveBtn.setVisibility(View.GONE);
            holder.deleteBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return articleList.size();
    }

    public static class ArticleViewHolder extends RecyclerView.ViewHolder {
        TextView title, content, author;
        ImageView image;
        Button approveBtn, deleteBtn;

        public ArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.articleTitle);
            content = itemView.findViewById(R.id.articleContent);
            author = itemView.findViewById(R.id.articleAuthor);
            image = itemView.findViewById(R.id.articleImage);
            approveBtn = itemView.findViewById(R.id.approveButton);
            deleteBtn = itemView.findViewById(R.id.deleteButton);
        }
    }

    public interface OnApproveClickListener {
        void onApproveClick(int position);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }
}
