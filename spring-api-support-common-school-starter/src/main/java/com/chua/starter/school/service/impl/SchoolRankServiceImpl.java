package com.chua.starter.school.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.school.entity.SchoolRank;
import com.chua.starter.school.mapper.SchoolRankMapper;
import com.chua.starter.school.service.SchoolRankService;

@Service
public class SchoolRankServiceImpl extends ServiceImpl<SchoolRankMapper, SchoolRank> implements SchoolRankService {

}
