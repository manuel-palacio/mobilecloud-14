/*
 * 
 * Copyright 2014 Manuel Palacio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.magnum.dataup;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class VideoFileService {

    private static final AtomicLong currentId = new AtomicLong(0L);

    private Map<Long, Video> videos = new ConcurrentHashMap<>();


    @RequestMapping(value = "/video", method = POST)
    public ResponseEntity<Video> addVideo(@RequestBody Video video, UriComponentsBuilder uriBuilder) {

        save(video);

        String uriString = uriBuilder.path("/video/{id}/data").buildAndExpand(video.getId()).toUriString();

        video.setDataUrl(uriString);

        return new ResponseEntity<>(video, HttpStatus.CREATED);
    }

    @RequestMapping(value = "video/{id}/data", method = POST)
    public VideoStatus addVideoData(@RequestParam("data") MultipartFile multipartFile,
                                    @PathVariable("id") Long id) throws IOException {

        if (!videos.containsKey(id)) {
            throw new VideoNotFoundException(id);
        }


        Video video = videos.get(id);


        VideoFileManager videoFileManager = VideoFileManager.get();

        videoFileManager.saveVideoData(video, multipartFile.getInputStream());

        return new VideoStatus(VideoStatus.VideoState.READY);
    }

    @RequestMapping(value = "video/{id}/data", method = GET)
    public void getVideoData(@PathVariable("id") Long id, HttpServletResponse response) throws IOException {

        if (!videos.containsKey(id)) {
            throw new VideoNotFoundException(id);
        }

        Video video = videos.get(id);
        VideoFileManager videoFileManager = VideoFileManager.get();

        try {
            videoFileManager.copyVideoData(video, response.getOutputStream());
            response.setContentType("video/mp4");
            response.flushBuffer();
        } catch (FileNotFoundException e) {
            throw new VideoNotFoundException(id);
        }
    }

    @RequestMapping(value = "/video", method = GET)
    public List<Video> getVideos() {
        return videos.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
    }

    public Video save(Video entity) {
        checkAndSetId(entity);
        videos.put(entity.getId(), entity);
        return entity;
    }

    private void checkAndSetId(Video entity) {
        if (entity.getId() == 0) {
            entity.setId(currentId.incrementAndGet());
        }
    }
}
