/*
 * 
 * Copyright 2014 Jules White
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
package org.magnum.mobilecloud.video;

import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.magnum.mobilecloud.video.controller.VideoFileManager;
import org.magnum.mobilecloud.video.model.AverageVideoRating;
import org.magnum.mobilecloud.video.model.Video;
import org.magnum.mobilecloud.video.model.VideoStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;


@Controller
public class VideoSvc {

	/**
	 * You will need to create one or more Spring controllers to fulfill the
	 * requirements of the assignment. If you use this file, please rename it
	 * to something other than "AnEmptyController"
	 * 
	 * 
		 ________  ________  ________  ________          ___       ___  ___  ________  ___  __       
		|\   ____\|\   __  \|\   __  \|\   ___ \        |\  \     |\  \|\  \|\   ____\|\  \|\  \     
		\ \  \___|\ \  \|\  \ \  \|\  \ \  \_|\ \       \ \  \    \ \  \\\  \ \  \___|\ \  \/  /|_   
		 \ \  \  __\ \  \\\  \ \  \\\  \ \  \ \\ \       \ \  \    \ \  \\\  \ \  \    \ \   ___  \  
		  \ \  \|\  \ \  \\\  \ \  \\\  \ \  \_\\ \       \ \  \____\ \  \\\  \ \  \____\ \  \\ \  \ 
		   \ \_______\ \_______\ \_______\ \_______\       \ \_______\ \_______\ \_______\ \__\\ \__\
		    \|_______|\|_______|\|_______|\|_______|        \|_______|\|_______|\|_______|\|__| \|__|
                                                                                                                                                                                                                                                                        
	 * 
	 */
	
	public static final String RATING_PARAMETER = "rating";
	
	public static final String DATA_PARAMETER = "data";

	public static final String ID_PARAMETER = "id";

	public static final String VIDEO_SVC_PATH = "/video";
	
	public static final String VIDEO_DATA_PATH = VIDEO_SVC_PATH + "/{id}/data";
	
	public static final String VIDEO_RATING_PATH = VIDEO_SVC_PATH + "/{id}/rating";
	
	public static final String VIDEO_RATING_SEND_PATH = VIDEO_SVC_PATH + "/{id}/rating/{rating}";
	
	private static final AtomicLong currentId = new AtomicLong(0L);
	
	private Map<Long,Video> videos = new HashMap<Long, Video>();
	
	@Autowired
	private VideoFileManager videoDataMgr;
	
	@RequestMapping(value=VIDEO_SVC_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList(){
		return videos.values();
	}
	
	@RequestMapping(value=VIDEO_SVC_PATH, method=RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v, Principal principal, 
										HttpServletResponse response){
		if(videos.containsKey(v.getId()))
		{
			Video currentVideo = videos.get(v.getId());
			if(!currentVideo.getOwner().equals(principal.getName()))
			{
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return null;
			}
			
		}
		else
		{
			checkAndSetId(v);
		}
		
		//v.setUrl(getDataUrl(v.getId()));
		v.setOwner(principal.getName());
		videos.put(v.getId(), v);
		
		return v;
	}
	
	@RequestMapping(value=VIDEO_DATA_PATH, method=RequestMethod.POST)
	public @ResponseBody VideoStatus setVideoData(@PathVariable(ID_PARAMETER) long id, 
												@RequestParam(DATA_PARAMETER) MultipartFile videoData,
												HttpServletResponse response,
												Principal principal) throws IOException{
		
		if(id==0||!videos.containsKey(id)||videos.get(id)==null){
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		
		if(!videos.get(id).getOwner().equals(principal.getName()))
		{
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		
		if(videoDataMgr==null)
		{
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;
		}
		
		videoDataMgr.saveVideoData(videos.get(id), videoData.getInputStream());
		
		return new VideoStatus(VideoStatus.VideoState.READY);
	}
	
	@RequestMapping(value=VIDEO_DATA_PATH, method=RequestMethod.GET)
	public void getData(@PathVariable(ID_PARAMETER) long id, 
						HttpServletResponse response) throws IOException{
		if(id==0||!videos.containsKey(id)||videos.get(id)==null){
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		if(videoDataMgr==null)
		{
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		
		videoDataMgr.copyVideoData(videos.get(id), response.getOutputStream());
	}
	
	@RequestMapping(value=VIDEO_RATING_SEND_PATH, method=RequestMethod.POST)
	public @ResponseBody AverageVideoRating setRating(@PathVariable(ID_PARAMETER) long id, 
										 @PathVariable(RATING_PARAMETER) double rating,
										 HttpServletResponse response,
										 Principal principal) throws IOException{
		
		
		if(id==0||!videos.containsKey(id)||videos.get(id)==null){
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		
		return videos.get(id).sendRating(principal.getName(), rating);
	}
	
	@RequestMapping(value=VIDEO_RATING_PATH, method=RequestMethod.GET)
	public @ResponseBody AverageVideoRating setRating(@PathVariable(ID_PARAMETER) long id,
										 HttpServletResponse response) throws IOException{
		
		
		if(id==0||!videos.containsKey(id)||videos.get(id)==null){
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		
		return videos.get(id).getRating();
	}
	
	private void checkAndSetId(Video entity) {
        if(entity.getId() == 0){
            entity.setId(currentId.incrementAndGet());
        }
    }
	
	
	private String getDataUrl(long videoId){
        String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
        return url;
    }

    private String getUrlBaseForLocalServer() {
       HttpServletRequest request = 
           ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
       String base = 
          "http://"+request.getServerName() 
          + ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
       return base;
    }
}
