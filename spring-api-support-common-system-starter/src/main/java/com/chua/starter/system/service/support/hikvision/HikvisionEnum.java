package com.chua.starter.system.service.support.hikvision;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 海康
 *
 * @author CH
 */
@AllArgsConstructor
@Getter
public enum HikvisionEnum {
    /**
     * 获取监控点在线状态
     * <p>
     * 获取监控点在线状态，返回结果分页展示。
     * </p>
     */
    ONLINE("/api/nms/v1/online/camera/get"),
    /**
     * 分页查询固定车辆信息
     * <p>
     * 分页查询固定车辆信息
     * </p>
     */
    VEHICLE_LIST("/api/resource/v2/vehicle/advance/vehicleList"),
    /**
     * 获取停车库列表
     * <p>
     * 根据停车场唯一表示集合获取停车列表信息
     * </p>
     */
    CAR_CARPORT("/api/resource/v1/park/parkList"),
    /**
     * 查询停车账单(根据停车库和车牌号)
     * <p>
     * </p>
     */
    CAR_CARPORT_BILL("/api/resource/v1/pay/quickPreBill"),
    /**
     * 获取监控点预览取流URLv2
     * <p>
     * 1.平台正常运行；平台已经添加过设备和监控点信息。
     * 2.平台需要安装mgc取流服务。
     * 3.通过openAPI获取到监控点数据，依据自身业务开发监控点导航界面。
     * 4.根据监控点编号调用本接口获取预览取流URL，协议类型包括：hik、rtsp、rtmp、hls、ws。
     * 5.通过开放平台的开发包进行实时预览或者使用标准的GUI播放工具进行实时预览。
     * 6.为保证数据的安全性，取流URL设有有效时间，有效时间为5分钟。
     * </p>
     */
    CAMERAS_PREVIEW_URLS("/api/video/v2/cameras/previewURLs"),
    /**
     * 分页获取监控点资源
     * <p>
     * 获取监控点列表接口可用来全量同步监控点信息，返回结果分页展示。
     * </p>
     */
    CAMERAS_PAGE_SEARCH("/api/resource/v1/cameras"),
    /**
     * 功能描述：该接口可以查询发生在门禁点上的人员出入事件，支持多个维度来查询，支持按
     * 时间、人员、门禁点、事件类型四个维度来查询；其中按事件类型来查询的方式，如果查询
     * 不到事件，存在两种情况，一种是该类型的事件没有发生过，所以查询不到，还有一种情况，
     * 该类型的事件发生过，但是由于门禁管理组件对该事件类型订阅配置处于关闭状态，所以不
     * 会存储该类型的事件，导致查询不到，对于这种情况，需要到门禁管理组件中，将该事件类
     * 型的订阅配置打开
     */
    DOOR_EVENT("/api/acs/v2/door/events"),
    /**
     * 根据条件查询目录下有权限的门禁设备列表。
     */
    DOOR_SEARCH("/api/resource/v2/acsDevice/search"),

    /**
     * 获取门禁事件的图片，重定向后返回图片流。
     */
    DOOR_PICTURE("/api/acs/v1/event/pictures"),
    /**
     * 获取监控点在线状态
     * <p>
     * 获取监控点在线状态，返回结果分页展示。
     * </p>
     */
    CAMERAS_STATUS("/api/nms/v1/online/camera/get"),
    /**
     * 根据编号获取监控点详细信息
     * <p>
     * 获取单个监控点信息是指根据监控点唯一标识来获取指定的监控点信息。
     * </p>
     */
    CAMERAS_INDEX_CODE("/api/resource/v1/cameras/indexCode"),
    /**
     * 查询监控点列表v2
     * <p>
     * 根据条件查询目录下有权限的监控点列表。
     * 当返回字段对应的值为空时，该字段不返回。
     * </p>
     */
    CAMERAS_SEARCH("/api/resource/v2/camera/search"),
    /*********************************************************/
    /**
     * 分页获取监控点资源
     * <p>
     * 获取监控点列表接口可用来全量同步监控点信息，返回结果分页展示。
     * </p>
     */
    REGION_NODES("/api/irds/v2/region/nodesByParams"),
    /**
     * 获取根区域信息
     */
    REGION_ROOT("/api/resource/v1/regions/root"),
    /**
     * 分页获取区域列表
     * <p>
     * 获取区域列表接口可用来全量同步区域信息，返回结果分页展示。
     * </p>
     */
    REGIONS("/api/resource/v1/regions"),
    /**
     * 添加权限配置
     * 接口说明
     * <p>
     * 权限配置支持按组织、人员和设备通道配置权限，适用综合大楼、学校、医院等场景。
     * 说明：权限配置数据采用异步分批入库方式，接口调用成功后返回权限配置单编号，在配置的过程中分批插入数据，只有当配置单结束时才能查询到完整的权限配置信息。相同的配置数据重复配置时，第一次配置生效后，后面相同的配置将自动过滤丢弃。
     * 合作方配置的tagId用于让多个应用共用出入控制权限服务时，用以区分各自的配置信息。
     * 注意点：不同业务组件设备通道隔离，应该根据业务场景使用不同的设备通道配置权限；如对相同的设备通道都有业务应用，那么人员隔离，应该根据场景使用不同的人员，否则会造成权限条目归属相互竞争的情况，在权限条目综合查询时，数据归属以最后一次入库配置为准。
     * 单次接口最多支持1000个设备资源和1000个人员数据。
     */
    AUTH_CONFIG_ADD("/api/acps/v1/auth_config/add"),
    /**
     * 根据区域编号获取下一级区域列表v2
     * <p>
     * 根据用户请求的资源类型和资源权限获取父区域的下级区域列表，主要用于逐层获取父区域的下级区域信息，例如监控点预览业务的区域树的逐层获取。下级区域只包括直接下级子区域。
     * 注：
     * 查询区域管理权限（resourceType为region），若父区域的子区域无权限、但是其孙区域有权限时，会返回该无权限的子区域，但是该区域的available标记为false（表示无权限）
     * </p>
     */
    REGIONS_SUB("/api/resource/v2/regions/subRegions"),
    /**
     * 根据编号获取区域详细信息
     * <p>
     * 根据区域编号查询区域详细信息及总条数，主要用于区域详细信息展示。
     * </p>
     */
    REGIONS_INFO("/api/resource/v1/region/regionCatalog/regionInfo"),


    /**
     * 查询组织列表v2
     * 根据不同的组织属性分页查询组织信息。
     * 查询组织列表接口可以根据组织唯一标识集、组织名称、组织状态这些查询条件来进行高级查询；若不指定查询条件，即全量获取所有的组织信息。返回结果分页展示。
     * 注：若指定多个查询条件，表示将这些查询条件进行“与”的组合后进行精确查询。
     * 根据“组织名称orgName”查询为模糊查询。
     * 根据父组织查询子孙组织，为便于构建组织树，会返回没有权限的父组织，通过available为false区分； 如果额外指定了其它字段，为在返回数据的基础上进一步过滤出符合条件的数据。
     */
    ORG_LIST("/api/resource/v2/org/advance/orgList"),

    /**
     * 根据资源类型分页获取资源列表，主要用于资源信息的全量同步。
     */
    DEVICE_RESOURCE_RESOURCES("/api/irds/v2/deviceResource/resources"),
    /**
     * a)一次性添加一张人脸，返回结果为添加成功的人脸。
     * b)添加的人脸图片，目前支持URL方式和二进制数据方式。URL方式时，人脸监控应用服务会通过HTTP协议的GET方式下载图片，校验图片，最后重新上传图片；二进制数据是指图片的字节流经过Base64编码后得到的字符串。
     * c)该URL能通过HTTP协议的GET方式能下载获取到即可，若包含认证，由调用方在URL中加上认证信息，保证URL能成功访问。
     * d)人脸监控应用服务会根据图片存储位置配置，将图片上传到图片存储服务器中，返回的URL为图片存储服务器上的相对地址。
     * e)图片大小和格式均有要求，上传大小在10KB到200KB间的图片，上传JPG格式的图片。
     * f)若添加的人脸对应的人脸分组已经配置有识别计划，则新添加的人脸会被一并下发到设备上。
     * g)该接口依赖于图片存储位置已配置完，请确保平台已经配置有人脸图片存储位置，否则添加必定失败。
     */
    FACE_ADDITION("/api/frs/v1/face/single/addition"),


    /**
     * a)根据人脸删除条件删除一批人脸，返回的data为布尔类型，true代表操作成功，false代表操作失败。
     * b)若删除的人脸对应的人脸分组已经配置有识别计划，则删除的人脸会被一并从设备上删除。
     * c)该接口是从指定分组内删除指定人脸。
     * d)人脸分组唯一标识和人脸的唯一标识集合均不能为空。
     * e)一次性最多从一个分组内删除1000个人脸。
     */
    FACE_DELETE("/api/frs/v1/face/deletion"),

    /**
     * a)修改单张人脸信息,返回的data为布尔类型，true代表操作成功，false代表操作失败。
     * b)若修改的人脸对应的人脸分组已经配置有识别计划，则修改后的人脸会被重新下发到设备上。
     * c)修改后的字段信息以修改时传入的字段为准，若不传字段或传入null字段，则该字段会被置为null。
     * d)如果要修改人脸信息，则需要传递faceInfo对象。
     * e)如果要修改人脸图片，则需要传递facePic对象。
     */
    FACE_UPDATE("/api/frs/v1/face/single/update"),

    /**
     * a)根据查询条件，批量查询人脸信息，一次性最多查询1000条人脸。
     * b)查询条件之间的关系为与，即所有条件同时生效。
     * c)可以通过传入faceGroupIndexCode，查询指定人脸分组下的人脸。
     */
    FACE_SEARCH("/api/frs/v1/face"),
    /**
     * 一次性添加一个人脸分组，返回结果为添加成功的人脸分组。
     */
    FACE_GROUP_ADDITION("/api/frs/v1/face/group/single/addition"),
    /**
     * a)根据删除条件，删除一批人脸分组,最大1000个，返回的data为布尔类型，true代表操作成功，false代表操作失败。
     * b)已经配置有识别计划的人脸分组，不允许删除，由特定错误码标识。
     * c)根据唯一标识删除时，会校验唯一标识的有效性，若任意分组唯一标识无效，则返回错误。传入的唯一标识重复，做去重处理。
     */
    FACE_GROUP_DELETION("/api/frs/v1/face/group/batch/deletion"),
    /**
     * a)修改单个人脸分组，返回的data为布尔类型，true代表操作成功，false代表操作失败。
     * b)修改后的字段信息以修改时传入的字段为准，若不传字段或传入null字段，则该字段会被置为null。
     * c)若该人脸分组已经被配置到识别计划中，则修改人脸分组后，会将修改后的信息，一并下发到设备上。
     */
    FACE_GROUP_UPDATE("/api/frs/v1/face/group/single/update"),
    /**
     * a)根据查询条件，查询人脸分组集合。
     */
    FACE_GROUP_SEARCH("/api/frs/v1/face/group"),
    /**
     * 资源属性接口
     */
    RESOURCE_PROPERTIES("/api/resource/v1/resource/properties"),
    /**
     * 人员添加
     * 添加人员信息接口，注意，在安保基础数据配置的必选字段必须都包括在入参中。
     * 人员添加的时候，可以指定人员personId，不允许与其他人员personId重复，包括已删除的人员。
     * 本接口支持人员信息的扩展字段，按照属性定义key:value上传即可， 可通过获取资源属性接口，获取平台已启用的人员属性信息。
     */
    PEOPLE_ADD("/api/resource/v2/person/single/add"),
    /**
     * 删除
     */
    PEOPLE_DELETE("/api/resource/v1/person/batch/delete"),
    /**
     * 添加人脸信息。
     */
    PEOPLE_FACE_ADD("/api/resource/v1/face/single/add"),
    /**
     * 人员查询
     * 获取人员列表接口可用来全量同步人员信息，返回结果分页展示。本接口支持自定义属性的返回， 通过获取资源属性接口获取平台已支持人员属性的说明。
     */
    PEOPLE_SEARCH("/api/resource/v2/person/personList"),


    RESOURCE_FACE_ADD("/api/resource/v1/face/single/add"),
    RESOURCE_FACE_DELETE("/api/resource/v1/face/single/delete"),
    CAR_SEARCH("/api/resource/v2/vehicle/advance/vehicleList"),
    DEVICE_SEARCH("/api/resource/v2/encodeDevice/search"),
    CROSS_RECORDS("/api/pms/v1/crossRecords/page"),
    CAR_ADD_OR_UPDATE("/api/resource/v1/vehicle/batch/add"),
    CAR_DELETE("/api/resource/v1/vehicle/batch/delete"),

    CAR_PART_LIST("/api/resource/v1/park/parkList"),
    CAR_CHARGE("/api/pms/v1/car/charge"),
    CAR_UN_CHARGE("/api/pms/v1/car/charge/deletion"),
    CAR_CHARGE_SEARCH("/api/pms/v1/car/charge/page"),
    ;

    private String url;

}
