package com.chua.starter.project.service.impl;

import com.chua.common.support.ai.DetectionConfiguration;
import com.chua.common.support.ai.Detector;
import com.chua.common.support.ai.result.PredictResult;
import com.chua.common.support.ai.result.PredictResultObject;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.deeplearning.support.ml.identification.IdentificationEngine;
import com.chua.starter.entity.project.SysAiModule;
import com.chua.starter.entity.project.SysProject;
import com.chua.starter.project.pojo.ImageRequest;
import com.chua.starter.project.service.SysAiModuleService;
import com.chua.starter.project.service.SysProjectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapSpotServiceImplTest {

    @Mock
    private SysAiModuleService sysAiModuleService;
    @Mock
    private SysProjectService sysProjectService;
    @Mock
    private IdentificationEngine identificationEngine;
    @Mock
    private Detector detector;

    @TempDir
    Path tempDir;

    @Test
    void shouldReturnAllCapSpotMatches(@TempDir Path dir) throws Exception {
        CapSpotServiceImpl service = new TestableCapSpotServiceImpl(sysAiModuleService, sysProjectService, identificationEngine);
        ImageRequest request = request();
        File templateFile = Files.createFile(dir.resolve("template.png")).toFile();
        File targetFile = Files.createFile(dir.resolve("target.png")).toFile();

        when(sysAiModuleService.getOneByCode(1, "capspot")).thenReturn(module());
        when(sysProjectService.getOne(1)).thenReturn(project());
        when(identificationEngine.get(isNull(), eq(Detector.class))).thenReturn(detector);

        PredictResult first = new PredictResult();
        first.setValue("img0");
        PredictResult second = new PredictResult();
        second.setValue("img1");
        PredictResultObject<PredictResult> expected = PredictResultObject.of(List.of(first, second), null);
        when(detector.detect(any())).thenReturn(expected);

        ReturnResult<PredictResultObject<PredictResult>> result = service.detect(templateFile, targetFile, request);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().size());

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(detector).detect(captor.capture());
        Map<?, ?> payload = (Map<?, ?>) captor.getValue();
        assertSame(templateFile, payload.get("template"));
        assertSame(targetFile, payload.get("target"));
    }

    @Test
    void shouldRejectMissingTargetFile() throws Exception {
        CapSpotServiceImpl service = new TestableCapSpotServiceImpl(sysAiModuleService, sysProjectService, identificationEngine);
        ImageRequest request = request();
        File templateFile = Files.createFile(tempDir.resolve("template-only.png")).toFile();

        when(sysAiModuleService.getOneByCode(1, "capspot")).thenReturn(module());
        when(sysProjectService.getOne(1)).thenReturn(project());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.detect(templateFile, null, request));
        assertEquals("目标文件不能为空", ex.getMessage());
    }

    @Test
    void shouldReturnEmptyWhenDetectorReturnsNull() throws Exception {
        CapSpotServiceImpl service = new TestableCapSpotServiceImpl(sysAiModuleService, sysProjectService, identificationEngine);
        ImageRequest request = request();
        File templateFile = Files.createFile(tempDir.resolve("template-empty.png")).toFile();
        File targetFile = Files.createFile(tempDir.resolve("target-empty.png")).toFile();

        when(sysAiModuleService.getOneByCode(1, "capspot")).thenReturn(module());
        when(sysProjectService.getOne(1)).thenReturn(project());
        when(identificationEngine.get(isNull(), eq(Detector.class))).thenReturn(detector);
        when(detector.detect(any())).thenReturn(null);

        ReturnResult<PredictResultObject<PredictResult>> result = service.detect(templateFile, targetFile, request);
        assertTrue(result.isSuccess());
        assertTrue(result.getData().isEmpty());
    }

    private ImageRequest request() {
        ImageRequest request = new ImageRequest();
        request.setRequestId("1");
        request.setModel("capspot");
        return request;
    }

    private SysAiModule module() {
        SysAiModule module = new SysAiModule();
        module.setSysAiModuleCode("capspot");
        module.setSysAiModuleType("image-matching");
        return module;
    }

    private SysProject project() {
        SysProject project = new SysProject();
        project.setSysProjectDictItemCode("onnx");
        project.setSysProjectEndpoint("http://localhost");
        project.setSysProjectLocalPath("H:/environment/cache/repo/model/undefined/ai/djl/localmodelzoo");
        return project;
    }

    private static final class TestableCapSpotServiceImpl extends CapSpotServiceImpl {
        private final IdentificationEngine identificationEngine;

        private TestableCapSpotServiceImpl(SysAiModuleService sysAiModuleService,
                                           SysProjectService sysProjectService,
                                           IdentificationEngine identificationEngine) {
            super(sysAiModuleService, sysProjectService);
            this.identificationEngine = identificationEngine;
        }

        @Override
        protected IdentificationEngine createIdentificationEngine(SysProject sysProject,
                                                                  DetectionConfiguration detectionConfiguration) {
            return identificationEngine;
        }
    }
}
