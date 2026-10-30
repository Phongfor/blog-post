package org.example.blogpost.specification;

import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import org.example.blogpost.entities.Post;
import org.example.blogpost.entities.User;
import org.springframework.data.jpa.domain.Specification;

public final class PostSpecification {

    private PostSpecification() {
    }

    // Lọc theo title: chứa chuỗi, không phân biệt hoa thường
    public static Specification<Post> titleContains(String title) {
        return (root, query, cb) -> {
            if (title == null || title.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(
                    cb.lower(root.get("title")),
                    "%" + escapeLike(title.trim().toLowerCase()) + "%",
                    '\\');
        };
    }

    // Luôn nạp author (JOIN FETCH, tránh N+1); nếu có author thì lọc theo username trên cùng join đó
    public static Specification<Post> withAuthor(String author) {
        return (root, query, cb) -> {
            Join<Post, User> authorJoin = authorJoin(root, query);
            if (author == null || author.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(
                    cb.lower(authorJoin.get("username")),
                    "%" + escapeLike(author.trim().toLowerCase()) + "%",
                    '\\');
        };
    }


    private static Join<Post, User> authorJoin(Root<Post> root, CriteriaQuery<?> query) {
        Class<?> resultType = query.getResultType();
        boolean isCountQuery = resultType == Long.class || resultType == long.class;
        if (isCountQuery) {
            return root.join("author", JoinType.INNER);
        }
        return (Join<Post, User>) root.<Post, User>join("author", JoinType.INNER);
    }

    // Escape ký tự đặc biệt của LIKE để từ khóa như "100%" không bị hiểu là wildcard
    private static String escapeLike(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}