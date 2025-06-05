/*
 * Copyright (c) 2019 Of Him Code Technology Studio
 * Jpom is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 * 			http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package com.chua.starter.monitor.jpom.controller;

import com.chua.report.client.starter.jpom.common.controller.BaseMyErrorController;
import com.chua.starter.monitor.jpom.common.interceptor.NotLogin;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author bwcx_jzy
 * @since 2022/4/16
 */
//@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
@NotLogin
public class MyErrorController extends BaseMyErrorController {
    public MyErrorController(ErrorAttributes errorAttributes) {
        super(errorAttributes);
    }
}
