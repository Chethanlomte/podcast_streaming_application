package com.chethan.PasswordMailOTP.service;

import com.chethan.PasswordMailOTP.entity.Podcast;
import com.chethan.PasswordMailOTP.repository.PodcastRepo;
import com.chethan.PasswordMailOTP.util.YouTubeDurationFetcher;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.springframework.transaction.annotation.Transactional;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class PodcastService {

    @Autowired
    private PodcastRepo podcastRepo;

    @Autowired
    private YouTubeDurationFetcher youTubeDurationFetcher;

    /**
     * Get all podcasts with pagination support
     * @param pageable pagination information (page number, page size, sorting)
     * @return Page containing the list of podcasts and pagination information
     */
    public Page<Podcast> getAllPodcasts(Pageable pageable) {
        return podcastRepo.findAll(pageable);
    }
    
    /**
     * Get all podcasts without pagination
     * @return List of all podcasts
     */
    public List<Podcast> getAllPodcastsList() {
        return podcastRepo.findAll();
    }

    public Optional<Podcast> getPodcast(Long id) {
        return podcastRepo.findById(id);
    }

    private boolean isYouTubeUrl(String url) {
        return url != null && (url.contains("youtube.com") || url.contains("youtu.be"));
    }

    @Transactional
    public Optional<Podcast> fetchAndSave(String url, String Title, String AuthorName, String Category, String Description) {

        if (url == null || url.isBlank() ||
                Title == null || Title.isBlank() ||
                AuthorName == null || AuthorName.isBlank() ||
                Category == null || Category.isBlank() ||
                Description == null || Description.isBlank()) {
            return Optional.empty(); // Controller should return 400 Bad Request
        }
        try {
            Podcast podcast = podcastRepo.findByRssUrl(url).orElseGet(Podcast::new);

            podcast.setRssUrl(url);
            podcast.setTitle(Title);
            podcast.setAuthorName(AuthorName);
            podcast.setCategory(Category);
            podcast.setDescription(Description);

            // Auto-handled fields from oEmbed
            JSONObject oembed = fetchYouTubeMetadata(url);
            if (oembed != null) {
                podcast.setAuthor(oembed.optString("author_name", "YouTube")); // Auto author
                podcast.setImageUrl(oembed.optString("thumbnail_url", null));  // Auto image
            } else {
                podcast.setAuthor("YouTube"); // fallback
                podcast.setImageUrl(null);
            }

            // 🔹 Fetch YouTube duration
            Double durationInSeconds = youTubeDurationFetcher.getVideoDurationInSeconds(url);
            podcast.setDuration(durationInSeconds);

            // Auto-handled fields
            podcast.setSourceType("YOUTUBE");
            podcast.setLastUpdated(LocalDateTime.now());

            return Optional.of(podcastRepo.save(podcast));
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch and save podcast: " + e.getMessage(), e);
        }
    }
    /**
     * Fetch YouTube metadata via oEmbed
     */
    private JSONObject fetchYouTubeMetadata(String videoUrl) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.youtube.com/oembed?url=" + videoUrl + "&format=json"))
                .header("Accept", "application/json")
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to fetch YouTube metadata. HTTP error code: " + response.statusCode());
            }
            
            return new JSONObject(response.body());
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch YouTube metadata: " + e.getMessage(), e);
        }
    }
    /**
     * Search podcasts by keyword with pagination support
     * @param keyword search term
     * @param pageable pagination information
     * @return Page containing matching podcasts and pagination info
     */
    public Page<Podcast> searchPodcasts(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return Page.empty(pageable);
        }
        return podcastRepo.search(keyword, pageable);
    }
    
    /**
     * Search podcasts by keyword without pagination
     * @param keyword search term
     * @return List of matching podcasts
     */
    public List<Podcast> searchPodcastsList(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        return podcastRepo.search(keyword);
    }

    @Transactional
    public Optional<Podcast> incrementViews(Long id) {
        return podcastRepo.findById(id).map(podcast -> {
            podcast.incrementViews();
            return podcastRepo.save(podcast);
        });
    }
    
    /**
     * Get podcasts by category with pagination
     * @param category category to filter by
     * @param pageable pagination information
     * @return Page of podcasts in the specified category
     */
    @Transactional(readOnly = true)
    public Page<Podcast> getPodcastsByCategory(String category, Pageable pageable) {
        if (category == null || category.isBlank()) {
            return Page.empty(pageable);
        }
        return podcastRepo.findByCategoryIgnoreCase(category, pageable);
    }
    
    /**
     * Get trending podcasts ordered by view count in descending order
     * @param limit maximum number of podcasts to return
     * @return List of trending podcasts
     */
    @Transactional(readOnly = true)
    public List<Podcast> getTrendingPodcasts(int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        // Use the existing repository method if the limit is 10
        if (limit == 10) {
            return podcastRepo.findTop10ByOrderByViewsDesc();
        }
        // For other limits, use a custom query with Pageable
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        return podcastRepo.findTopNByOrderByViewsDesc(pageable);
    }

    public String deletePodcast(Long podId) {
        Podcast podcast = podcastRepo.findById(podId)
                .orElseThrow(()-> new RuntimeException("Podcast Not found in the Podcast Application"));
        if (podcast.getPlaylists() != null) {
            podcast.getPlaylists().forEach(playlist -> playlist.getPodcasts().remove(podcast));
            podcast.getPlaylists().clear();
        }

        podcastRepo.delete(podcast);
        return "Podcast deleted Successfully!";
    }

    public List<String> getAllCategories(){
        return podcastRepo.findAllDistinctCategories();
    }
}
