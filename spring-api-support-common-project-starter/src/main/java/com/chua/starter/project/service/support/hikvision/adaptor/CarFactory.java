package com.chua.starter.project.service.support.hikvision.adaptor;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.chua.starter.project.service.support.hikvision.HikvisionClient;
import com.chua.starter.project.service.support.hikvision.result.*;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static com.chua.starter.project.service.support.hikvision.HikvisionEnum.*;


/**
 * 车辆/车库适配器
 *
 * @author CH
 */
public class CarFactory {

    private final HikvisionClient hikvisionClient;

    public CarFactory(HikvisionClient hikvisionClient) {
        this.hikvisionClient = hikvisionClient;
    }


    /**
     * 分页查询固定车辆信息
     * <p>
     * 分页查询固定车辆信息，返回结果分页展示。
     * </p>
     *
     * @param page     页码(1)
     * @param pageSize 分页数量 (1000)
     */
    public HikCarInfo vehicleList(int page, int pageSize) {
        JSONObject json = new JSONObject();
        json.put("pageNo", page);
        json.put("pageSize", pageSize);
        json.put("isBandPerson", 1);
        try {
            return hikvisionClient.executePost(VEHICLE_LIST, json, HikCarInfo.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 分页获取监控点资源
     * <p>
     * 获取监控点列表接口可用来全量同步监控点信息，返回结果分页展示。
     * </p>
     *
     * @param parkIndexCodes 停车场唯一标识集合多个值使用英文逗号分隔,不超过1000个（选填）
     */
    public HikCarCarport carCarport(String... parkIndexCodes) {
        JSONObject json = new JSONObject();
        json.put("parkIndexCodes", parkIndexCodes);
        try {
            return hikvisionClient.executePost(CAR_CARPORT, json, HikCarCarport.class);
        } catch (Exception e) {
            e.printStackTrace();
            return HikCarCarport.empty();
        }
    }

    /**
     * 查询停车账单(根据停车库和车牌号)
     * <p>
     * </p>
     *
     * @param parkSysCode 停车场唯一标识（选填）
     * @param plateNo     车牌号
     */
    public HikCarCarportBill carCarportBill(String parkSysCode, String plateNo) {
        if (StringUtils.isBlank(plateNo)) {
            throw new RuntimeException("车牌号不能为空");
        }
        JSONObject json = new JSONObject();
        json.put("parkSyscode", parkSysCode);
        json.put("plateNo", plateNo);
        try {
            return hikvisionClient.executePost(CAR_CARPORT_BILL, json, HikCarCarportBill.class);
        } catch (Exception e) {
            e.printStackTrace();
            return HikCarCarportBill.empty();
        }
    }

    /**
     * 查询设备
     * <p>
     * </p>
     *
     * @param page     页码(1)
     * @param pageSize 分页数量 (1000)
     */
    public CarSearchResult carSearch(int page, int pageSize) {
        JSONObject json = new JSONObject();
        json.put("pageNo", page);
        json.put("pageSize", pageSize);
        try {
            return hikvisionClient.executePost(CAR_SEARCH, json, CarSearchResult.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 查询车辆(根据停车库和车牌号)
     * <p>
     * </p>
     *
     * @param page     页码(1)
     * @param pageSize 分页数量 (1000)
     */
    public String deviceSearch(int page, int pageSize) {
        JSONObject json = new JSONObject();
        json.put("pageNo", page);
        json.put("pageSize", pageSize);
        try {
            return hikvisionClient.executePost(DEVICE_SEARCH, json, String.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 查询车辆(根据停车库和车牌号)
     * <p>
     * </p>
     *
     * @param page      页码(1)
     * @param pageSize  分页数量 (1000)
     * @param startTime
     * @param endTime
     */
    public CrossRecords crossRecords(int page, int pageSize, String startTime, String endTime) {
        JSONObject json = new JSONObject();
        json.put("pageNo", page);
        json.put("pageSize", pageSize);
        json.put("startTime", startTime);
        json.put("endTime", endTime);
        try {
            return hikvisionClient.executePost(CROSS_RECORDS, json, CrossRecords.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 添加车辆(根据停车库和车牌号)
     * <p>
     * </p>
     *
     * @param vehicleId 车牌ID
     * @param plateNo   车牌
     */
    public CarAddResult carAddOrUpdate(String vehicleId, String plateNo) {
        JSONArray json = new JSONArray();
        json.add(new JSONObject()
                .fluentPut("clientId", vehicleId)
                .fluentPut("plateNo", plateNo)
        );
        try {
            return hikvisionClient.executePost(CAR_ADD_OR_UPDATE, json, CarAddResult.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 删除车辆(根据停车库和车牌号)
     * <p>
     * </p>
     *
     * @param vehicleId 车牌ID
     */
    public HikResult carDelete(String[] vehicleId) {
        JSONObject json = new JSONObject();
        json.put("vehicleIds", vehicleId);
        try {
            return hikvisionClient.executePost(CAR_DELETE, json, HikResult.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<CarPartResult> carPart() {
        JSONObject json = new JSONObject();
        try {
            return JSON.parseArray(hikvisionClient.executePost(CAR_PART_LIST, json, String.class), CarPartResult.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public HikResult carUnCharge(String plateNo) {
        JSONObject json = new JSONObject();
        json.put("plateNo", plateNo);
        try {
            return hikvisionClient.executePost(CAR_UN_CHARGE, json, HikResult.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public HikResult carCharge(String parkSyscode, String plateNo, String startTime, String endTime) {
        JSONObject json = new JSONObject();
        json.put("plateNo", plateNo);
        json.put("parkSyscode", parkSyscode);
        json.put("startTime", startTime);
        json.put("endTime", endTime);
        try {
            return hikvisionClient.executePost(CAR_CHARGE, json, HikResult.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public CarChargeResult carChargeSearch(int pageNo, int pageSize) {
        JSONObject json = new JSONObject();
        json.put("pageNo", pageNo);
        json.put("pageSize", pageSize);
        try {
            return hikvisionClient.executePost(CAR_CHARGE_SEARCH, json, CarChargeResult.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
