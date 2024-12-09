package com.example.HeygenAPI.controller;

import com.example.HeygenAPI.model.Job;
import com.example.HeygenAPI.response.Response;
import com.example.HeygenAPI.service.VideoService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class VideoController {

    @Autowired
    VideoService videoService;

    @PostMapping("/translateVideo")
    public Response translateVideo(@RequestBody Job job) throws BadRequestException {
        String status = videoService.translateVideo(job);
        return new Response(status);
    }

    @GetMapping("/status/{jobId}")
    public Response getStatus(@PathVariable("jobId") String jobId) throws RuntimeException{
        String status = videoService.getStatus(jobId);
        return new Response(status);
    }

    @PostMapping("/delete/{jobId}")
    public Response deleteVideo(@PathVariable("jobId") String jobId){
        String status = videoService.deleteVideo(jobId);
        return new Response(status);
    }


}
