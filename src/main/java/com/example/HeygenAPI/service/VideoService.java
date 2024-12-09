package com.example.HeygenAPI.service;

import com.example.HeygenAPI.model.Job;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class VideoService {

    public static Map<String, String> videos = new HashMap<>();

    public static final Logger logger = LoggerFactory.getLogger(VideoService.class);

    // Variable to store how much time it require to translate a video.
    @Value("${video.translation.timeRequired}")
    Long timeRequired;

    public String translateVideo(Job job) throws BadRequestException {
        logger.info("Translating the Video");
        if(videos.containsKey(job.getJobId())){
            logger.error("Job Id already exists.");
            throw new BadRequestException("Job Already Exists");
        }
        videos.put(job.getJobId(), "pending");

        // ExecutorService to manage job and monitoring threads
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Main job thread
        Future<?> jobFuture = executor.submit(() -> {
            try {
                performJob(job.getJobId());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Interruption happened while translating video - "+e.getLocalizedMessage());
                videos.put(job.getJobId(), "error");
            }
        });

        // Monitoring thread
        Future<?> monitorFuture = executor.submit(() -> {
            try {
                long startTime = System.currentTimeMillis();
                logger.info("Video Translation Start Time - "+startTime);
                while (true) {
                    String currentStatus = videos.get(job.getJobId());

                    // Check for error status
                    if (currentStatus.equals("error")) {
                        // Interrupt the main job thread
                        logger.info("Error occured while translating the video.");
                        jobFuture.cancel(true);
                        videos.put(job.getJobId(),"error");
                        break;
                    }

                    // Check for completion
                    if (currentStatus.equals("completed")) {
                        logger.info("Translation Job completed.");
                        videos.put(job.getJobId(),"completed");
                        break;
                    }

                    // Sleep between checks
                    Thread.sleep(1000);
                    logger.debug("Current Status for Job -> "+currentStatus);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Interruption Happened while monitoring the job.");
                videos.put(job.getJobId(),"error");
            } finally {
                // Shutdown executor to release resources
                logger.info("Shutting down the executor.");
                executor.shutdownNow();
            }
        });

        try {
            // Wait for monitoring to complete
            monitorFuture.get();

            // Return the result
            return videos.get(job.getJobId());
        } catch (Exception e) {
            logger.error("Error occurred while translating video -> "+e.getMessage());
        }
        return "translated";
    }

    // Method where we can perform our translation, for now making it sleep to simulate translating time
    private void performJob(String jobId) throws InterruptedException {
        logger.info("Performing Translation...");
        videos.put(jobId, "pending");

        for (int i = 0; i < timeRequired; i++) {

            Thread.sleep(1000);

            if (Thread.currentThread().isInterrupted()) {
                logger.error("Interruption while performing the Job.");
                videos.put(jobId, "error");
                throw new InterruptedException("Job was interrupted");
            }
        }

        // Mark job as completed
        videos.put(jobId, "completed");
    }

    public String getStatus(String jobId) throws RuntimeException{
        logger.info("Getting the status of Job with jobId - "+jobId);
        if(!videos.containsKey(jobId)){
            logger.error("There is no jobs with Id - "+jobId);
            throw new RuntimeException("Error Job Id not found.");
        }
        return videos.get(jobId);
    }

    public String deleteVideo(String jobId){
        logger.info("Deleting the Video.");
        videos.put(jobId,"error");
        return "deleted";
    }
}
