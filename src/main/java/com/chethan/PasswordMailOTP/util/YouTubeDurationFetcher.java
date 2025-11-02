package com.chethan.PasswordMailOTP.util;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class YouTubeDurationFetcher {

    /**
     * Fetch video duration from YouTube HTML (no API key needed).
     */
    public Double getVideoDurationInSeconds(String videoUrl){
        try{
            RestTemplate restTemplate = new RestTemplate();
            String html = restTemplate.getForObject(videoUrl, String.class);

            // Find "approxDurationMs" from YouTube page HTML (milliseconds)
            Pattern pattern = Pattern.compile("\"approxDurationMs\":\"(\\d+)\"");
            Matcher matcher = pattern.matcher(html);

            if (matcher.find()) {
                long durationMs = Long.parseLong(matcher.group(1));
                return durationMs / 1000.0;  // Convert to seconds
            }

            // Fallback: check ISO duration format if exists
            pattern = Pattern.compile("\"lengthText\"\\s*:\\s*\\{\"simpleText\":\"(\\d+:\\d+(:\\d+)?)\"");
            matcher = pattern.matcher(html);
        }catch (Exception e){
            System.err.println("Failed to fetch YouTube duration: " + e.getMessage());
        }
        return null;
    }

    /**
     * Converts "MM:SS" or "HH:MM:SS" to seconds
     */

    private Double parseHumanDuration(String durationStr) {
        String[] parts = durationStr.split(":");
        int seconds = 0;
        if (parts.length == 2) { // MM:SS
            seconds = Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
        } else if (parts.length == 3) { // HH:MM:SS
            seconds = Integer.parseInt(parts[0]) * 3600
                    + Integer.parseInt(parts[1]) * 60
                    + Integer.parseInt(parts[2]);
        }
        return (double) seconds;
    }
}
