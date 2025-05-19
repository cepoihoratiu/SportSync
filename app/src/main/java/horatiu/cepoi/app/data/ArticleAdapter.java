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

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder> {

    private final List<Article> articleList;
    private final String userId;
    private final OnApproveToggleClickListener approveToggleClickListener;

    public interface OnApproveToggleClickListener {
        void onApproveToggle(Article article);
    }

    public ArticleAdapter(List<Article> articles, String userId, OnApproveToggleClickListener listener) {
        this.articleList = articles;
        this.userId = userId;
        this.approveToggleClickListener = listener;
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_article, parent, false);
        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        Article article = articleList.get(position);
        holder.title.setText(article.title);
        holder.content.setText(article.content);
        holder.author.setText("By: " + article.authorName);
        Glide.with(holder.itemView.getContext()).load(article.imageUrl).into(holder.image);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(article.content));
            v.getContext().startActivity(intent);
        });

        if ("tE6ixjCwOybj2IBD9XfE".equals(userId)) {
            holder.approveButton.setVisibility(View.VISIBLE);
            holder.approveButton.setText(article.approved ? "Approved ✅" : "Unapproved ❌");
            holder.approveButton.setBackgroundColor(article.approved ? Color.GREEN : Color.RED);
            holder.approveButton.setOnClickListener(v -> approveToggleClickListener.onApproveToggle(article));
        } else {
            holder.approveButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return articleList.size();
    }

    static class ArticleViewHolder extends RecyclerView.ViewHolder {
        TextView title, content, author;
        ImageView image;
        Button approveButton;

        public ArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.articleTitle);
            content = itemView.findViewById(R.id.articleContent);
            author = itemView.findViewById(R.id.articleAuthor);
            image = itemView.findViewById(R.id.articleImage);
            approveButton = itemView.findViewById(R.id.approveButton);
        }
    }
}
