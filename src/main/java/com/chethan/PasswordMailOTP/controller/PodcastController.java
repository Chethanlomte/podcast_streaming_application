package com.chethan.PasswordMailOTP.controller;

import com.chethan.PasswordMailOTP.dto.ApiResponse;
import com.chethan.PasswordMailOTP.entity.Podcast;
import com.chethan.PasswordMailOTP.exception.ResourceNotFoundException;
import com.chethan.PasswordMailOTP.service.PodcastService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/podcasts")
public class PodcastController {

    @Autowired
    private PodcastService podcastService;

    /**
     * Get all podcasts with pagination
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllPodcasts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
            
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Podcast> podcastPage = podcastService.getAllPodcasts(pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("podcasts", podcastPage.getContent());
        response.put("currentPage", podcastPage.getNumber());
        response.put("totalItems", podcastPage.getTotalElements());
        response.put("totalPages", podcastPage.getTotalPages());
        
        return ResponseEntity.ok(
            ApiResponse.success("Podcasts retrieved successfully", response)
        );
    }

    /**
     * Get podcast by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Podcast>> getPodcastById(@PathVariable Long id) {
        return podcastService.getPodcast(id)
            .map(podcast -> ResponseEntity.ok(
                ApiResponse.success("Podcast retrieved successfully", podcast)
            ))
            .orElseThrow(() -> new ResourceNotFoundException("Podcast not found with id: " + id));
    }

    /**
     * Fetch and save podcast from RSS URL
     */
    @PostMapping("/rssUrl")
    public ResponseEntity<ApiResponse<Podcast>> fetchPodcast(
            @Valid @RequestBody Map<String, String> body) {

        String rssUrl = body.get("rssUrl");
        String title = body.get("title");
        String authorName = body.get("authorName");
        String category = body.get("category");
        String description = body.get("description");

        // Validate required fields
        if (rssUrl == null || rssUrl.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("YouTube URL (rssUrl) is required"));
        }
        if (!rssUrl.contains("youtube.com") && !rssUrl.contains("youtu.be")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Only YouTube URLs are supported"));
        }
        if (title == null || title.isBlank() ||
                authorName == null || authorName.isBlank() ||
                category == null || category.isBlank() ||
                description == null || description.isBlank()) {

            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Fields title, authorName, category, and description are required"));
        }

        return podcastService.fetchAndSave(rssUrl, title, authorName, category, description)
                .map(podcast -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success("Podcast fetched and saved successfully", podcast)))
                .orElse(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to save the YouTube podcast")));
    }

    /**
     * Search podcasts by keyword
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Map<String, Object>>> searchPodcasts(
            @RequestParam @NotBlank String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
                
        Pageable pageable = PageRequest.of(page, size);
        Page<Podcast> results = podcastService.searchPodcasts(keyword, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("results", results.getContent());
        response.put("currentPage", results.getNumber());
        response.put("totalItems", results.getTotalElements());
        response.put("totalPages", results.getTotalPages());
        
        return ResponseEntity.ok(
            ApiResponse.success("Search results retrieved successfully", response)
        );
    }

    /**
     * Increment play count for a podcast
     */
    @GetMapping("/{id}/play")
    public ResponseEntity<ApiResponse<Podcast>> playPodcast(@PathVariable Long id) {
        return podcastService.incrementViews(id)
            .map(podcast -> ResponseEntity.ok(
                ApiResponse.success("Play count updated successfully", podcast)
            ))
            .orElseThrow(() -> new ResourceNotFoundException("Podcast not found with id: " + id));
    }

    /**
     * Get trending podcasts
     */
    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<List<Podcast>>> getTrendingPodcasts(
            @RequestParam(defaultValue = "10") int limit) {
                
        List<Podcast> trending = podcastService.getTrendingPodcasts(limit);
        return ResponseEntity.ok(
            ApiResponse.success("Trending podcasts retrieved successfully", trending)
        );
    }
    
    /**
     * Get podcasts by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPodcastsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
                
        Pageable pageable = PageRequest.of(page, size);
        Page<Podcast> podcasts = podcastService.getPodcastsByCategory(category, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("podcasts", podcasts.getContent());
        response.put("currentPage", podcasts.getNumber());
        response.put("totalItems", podcasts.getTotalElements());
        response.put("totalPages", podcasts.getTotalPages());
        
        return ResponseEntity.ok(
            ApiResponse.success("Podcasts retrieved by category successfully", response)
        );
    }

    @GetMapping("/deletePodcast/{podId}")
    public String deletePodcast(@PathVariable Long podId){
        return podcastService.deletePodcast(podId);
    }

    @GetMapping("/categories")
    public ResponseEntity<?> getAllCategories(){
        List<String> categories = podcastService.getAllCategories();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", categories.size());
        response.put("categories", categories);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/categories/{category}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPodcastsByCategories(
            @PathVariable String category,
            @RequestParam(defaultValue = "0")int page,
            @RequestParam(defaultValue = "10")int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<Podcast> podcasts = podcastService.getPodcastsByCategory(category, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("podcast",podcasts.getContent());
        response.put("currentPage", podcasts.getNumber());
        response.put("totalItems", podcasts.getTotalElements());
        response.put("totalPages", podcasts.getTotalPages());

        return ResponseEntity.ok(ApiResponse.success("Podcasts retrieved successfully by category", response));
    }

    @GetMapping("/featured")
    public ResponseEntity<List<Podcast>> getFeaturedPodcast()
    {
        List<Podcast> featured = podcastService.getFeaturedPodcasts();
        return ResponseEntity.ok(featured);
    }

    @GetMapping("/recentlyAdded")
    public ResponseEntity<List<Podcast>> getRecentlyAddedPodcasts(){
        List<Podcast> recent = podcastService.findAllByOrderByLastUpdatedDesc();
        return ResponseEntity.ok(recent);
    }
    
}
