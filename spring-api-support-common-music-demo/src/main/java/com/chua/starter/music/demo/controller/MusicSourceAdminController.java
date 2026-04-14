package com.chua.starter.music.demo.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.music.demo.service.MusicSourceSettingsService;
import com.chua.starter.music.model.MusicSourceOption;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/music/admin/sources")
public class MusicSourceAdminController {

    private final MusicSourceSettingsService musicSourceSettingsService;

    @GetMapping
    public ReturnResult<List<MusicSourceOption>> listSources() {
        return ReturnResult.ok(musicSourceSettingsService.listAllSources());
    }

    @PutMapping("/{sourceCode}")
    public ReturnResult<MusicSourceOption> updateSourceState(@PathVariable String sourceCode,
                                                             @RequestBody UpdateMusicSourceStateRequest request) {
        return ReturnResult.ok(musicSourceSettingsService.updateSourceState(sourceCode, request.enabled()));
    }

    public record UpdateMusicSourceStateRequest(boolean enabled) {
    }
}
