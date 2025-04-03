package com.chua.starter.video.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.video.entity.VideoMark;
import com.chua.starter.video.mapper.VideoMarkMapper;
import com.chua.starter.video.service.VideoMarkService;
import org.springframework.stereotype.Service;

@Service
public class VideoMarkServiceImpl extends ServiceImpl<VideoMarkMapper, VideoMark> implements VideoMarkService {

}
