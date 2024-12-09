package com.example.HeygenAPI;

import com.example.HeygenAPI.model.Job;
import com.example.HeygenAPI.response.Response;
import com.example.HeygenAPI.service.VideoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class HeygenApiApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private VideoService videoService;

	@Autowired
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		VideoService.videos.clear(); // Clear the video map before each test
	}

	@Test
	void testTranslateVideoSuccess() throws Exception {
		Job job = new Job("job1");
		mockMvc.perform(post("/translateVideo")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(job)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("completed"));
	}

	@Test
	void testTranslateVideoJobAlreadyExists() throws Exception {
		Job job = new Job("job1");
		VideoService.videos.put("job1", "pending"); // Simulate existing job

		mockMvc.perform(post("/translateVideo")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(job)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void testGetStatusSuccess() throws Exception {
		VideoService.videos.put("job1", "completed"); // Simulate a job

		mockMvc.perform(get("/status/job1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("completed"));
	}

	@Test
	void testGetStatusNotFound() throws Exception {
		mockMvc.perform(get("/status/nonExistingJob"))
				.andExpect(status().isInternalServerError())
				.andExpect(content().string("Error Job Id not found."));
	}

	@Test
	void testDeleteVideo() throws Exception {
		VideoService.videos.put("job1", "completed"); // Simulate an existing job

		mockMvc.perform(post("/delete/job1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("deleted"));

		// Assert that the job status is updated to "error"
		assert VideoService.videos.get("job1").equals("error");
	}
}
