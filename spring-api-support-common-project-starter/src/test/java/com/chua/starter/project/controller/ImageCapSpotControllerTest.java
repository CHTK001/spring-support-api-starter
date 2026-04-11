package com.chua.starter.project.controller;

import com.chua.common.support.ai.result.PredictResult;
import com.chua.common.support.ai.result.PredictResultObject;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.project.pojo.ImageRequest;
import com.chua.starter.project.service.CapSpotService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageCapSpotControllerTest {

    @Mock
    private CapSpotService capSpotService;

    @Test
    void shouldDelegateMultipartFilesToService() {
        ImageCapSpotController controller = new ImageCapSpotController(capSpotService);
        ImageRequest request = request();
        MockMultipartFile template = new MockMultipartFile(
                "template",
                "template.png",
                "image/png",
                "template".getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile target = new MockMultipartFile(
                "target",
                "target.png",
                "image/png",
                "target".getBytes(StandardCharsets.UTF_8)
        );

        PredictResult predictResult = new PredictResult();
        predictResult.setValue("spot-1");
        PredictResultObject<PredictResult> payload = PredictResultObject.of(List.of(predictResult), null);
        ReturnResult<PredictResultObject<PredictResult>> expected = ReturnResult.success(payload);
        when(capSpotService.detect(any(File.class), any(File.class), same(request))).thenReturn(expected);

        ReturnResult<PredictResultObject<PredictResult>> result = controller.detect(template, target, request);
        assertSame(expected, result);

        ArgumentCaptor<File> templateCaptor = ArgumentCaptor.forClass(File.class);
        ArgumentCaptor<File> targetCaptor = ArgumentCaptor.forClass(File.class);
        verify(capSpotService).detect(templateCaptor.capture(), targetCaptor.capture(), same(request));
        assertTrue(templateCaptor.getValue().isFile());
        assertTrue(targetCaptor.getValue().isFile());

        templateCaptor.getValue().delete();
        targetCaptor.getValue().delete();
    }

    @Test
    void shouldReturnIllegalWhenServiceThrows() {
        ImageCapSpotController controller = new ImageCapSpotController(capSpotService);
        ImageRequest request = request();
        MockMultipartFile template = new MockMultipartFile(
                "template",
                "template.png",
                "image/png",
                "template".getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile target = new MockMultipartFile(
                "target",
                "target.png",
                "image/png",
                "target".getBytes(StandardCharsets.UTF_8)
        );

        when(capSpotService.detect(any(File.class), any(File.class), same(request)))
                .thenThrow(new RuntimeException("boom"));

        ReturnResult<PredictResultObject<PredictResult>> result = controller.detect(template, target, request);
        assertFalse(result.isSuccess());
        assertEquals("CapSpot 模板点击定位失败: boom", result.getMsg());
    }

    private ImageRequest request() {
        ImageRequest request = new ImageRequest();
        request.setRequestId("1");
        request.setModel("capspot");
        return request;
    }
}
