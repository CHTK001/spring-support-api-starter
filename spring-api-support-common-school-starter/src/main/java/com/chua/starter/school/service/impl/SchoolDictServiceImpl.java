package com.chua.starter.school.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.school.mapper.SchoolDictMapper;
import com.chua.starter.school.entity.SchoolDict;
import com.chua.starter.school.service.SchoolDictService;

@Service
public class SchoolDictServiceImpl extends ServiceImpl<SchoolDictMapper, SchoolDict> implements SchoolDictService {

}
