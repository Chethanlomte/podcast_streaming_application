package com.chethan.PasswordMailOTP.repository;

import com.chethan.PasswordMailOTP.entity.Podcast;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PodcastRepo extends JpaRepository<Podcast, Long> {
    Optional<Podcast> findByRssUrl(String rssUrl);

    @Query("SELECT p FROM Podcast p " +
            "WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(p.author) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
            " OR LOWER(p.category) LIKE LOWER(CONCAT('%', :keyword, '%'))" )
    List<Podcast> search(@Param("keyword") String keyword);

    @Query("SELECT p FROM Podcast p " +
            "WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(p.author) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(p.category) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Podcast> search(@Param("keyword") String keyword, Pageable pageable);

    List<Podcast> findTop10ByOrderByViewsDesc();
    
    /**
     * Find podcasts by category with pagination
     * @param category category to filter by
     * @param pageable pagination information
     * @return Page of podcasts in the specified category
     */
    @Query("SELECT DISTINCT p FROM Podcast p LEFT JOIN FETCH p.favorites WHERE LOWER(p.category) = LOWER(:category)")
    Page<Podcast> findDistinctByCategoryIgnoreCase(@Param("category") String category, Pageable pageable);



    /**
     * Find top N podcasts ordered by view count in descending order
     * @param limit maximum number of podcasts to return
     * @return List of top N podcasts by views
     */
    @Query(value = "SELECT p FROM Podcast p ORDER BY p.views DESC")
    List<Podcast> findTopNByOrderByViewsDesc(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT DISTINCT p.category FROM Podcast p WHERE p.category IS NOT NULL")
    List<String> findAllDistinctCategories();

}