package com.chua.starter.collect.engine.spi;

import com.chua.starter.collect.engine.CollectEngine;

import java.util.ArrayList;
import java.util.List;

/**
 * 节点元数据构造工具。
 *
 * <p>把每个可执行节点都要暴露的循环四参数集中一处，新增节点类型不需要重复抄写，
 * 设计器的属性面板因此对所有节点保持一致的循环配置区。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
public final class CollectShapeMetas {

    private CollectShapeMetas() {
    }

    /**
     * 构造节点元数据骨架，已带上循环参数。
     *
     * @param shape       节点类型
     * @param label       显示名称
     * @param group       分组：来源/解析/控制/输出
     * @param icon        图标标识
     * @param description 功能说明
     * @return 节点元数据
     */
    public static CollectShapeMeta of(String shape, String label, String group, String icon, String description) {
        CollectShapeMeta meta = new CollectShapeMeta();
        meta.setShape(shape);
        meta.setLabel(label);
        meta.setGroup(group);
        meta.setIcon(icon);
        meta.setDescription(description);
        meta.setParams(loopParams());
        return meta;
    }

    /**
     * 所有可执行节点共用的循环参数。
     *
     * @return 循环参数列表
     */
    public static List<CollectParamMeta> loopParams() {
        List<CollectParamMeta> params = new ArrayList<>();
        params.add(text(CollectEngine.PARAM_LOOP_COUNT, "循环集合/次数", "textarea",
                "填次数如 10，或填集合表达式如 ${urlList}、${rows}；留空只执行一次"));
        CollectParamMeta index = text(CollectEngine.PARAM_LOOP_INDEX, "下标变量名", "text",
                "循环下标写入的变量名，留空用 index");
        index.setDefaultValue("index");
        params.add(index);
        CollectParamMeta item = text(CollectEngine.PARAM_LOOP_ITEM_VARIABLE, "元素变量名", "text",
                "当前迭代元素写入的变量名，下游节点用 ${item} 引用");
        item.setDefaultValue("item");
        params.add(item);
        CollectParamMeta parallel = option(CollectEngine.PARAM_LOOP_PARALLEL, "迭代并行", "0",
                "选 1 时各迭代同时入池，由流程线程数限流；下游按迭代各自展开", "0", "1");
        params.add(parallel);
        return params;
    }

    /**
     * 构造带默认值与提示的下拉参数。
     *
     * @param name         参数名
     * @param label        显示标题
     * @param defaultValue 默认值
     * @param tip          填写提示
     * @param options      候选项
     * @return 参数元数据
     */
    public static CollectParamMeta option(String name, String label, String defaultValue, String tip,
                                          String... options) {
        CollectParamMeta meta = CollectParamMeta.select(name, label, options);
        meta.setDefaultValue(defaultValue);
        meta.setTip(tip);
        return meta;
    }

    /**
     * 构造文本类参数并登记默认值。
     *
     * @param name         参数名
     * @param label        显示标题
     * @param type         控件类型
     * @param tip          填写提示
     * @return 参数元数据
     */
    public static CollectParamMeta text(String name, String label, String type, String tip) {
        CollectParamMeta meta = CollectParamMeta.of(name, label, type);
        meta.setTip(tip);
        return meta;
    }

    /**
     * 构造必填文本参数。
     *
     * @param name  参数名
     * @param label 显示标题
     * @param type  控件类型
     * @param tip   填写提示
     * @return 参数元数据
     */
    public static CollectParamMeta required(String name, String label, String type, String tip) {
        CollectParamMeta meta = text(name, label, type, tip);
        meta.setRequired(true);
        return meta;
    }

    /**
     * 追加参数并返回元数据，便于链式声明。
     *
     * @param meta   节点元数据
     * @param params 待追加参数
     * @return 入参元数据
     */
    public static CollectShapeMeta with(CollectShapeMeta meta, CollectParamMeta... params) {
        for (CollectParamMeta param : params) {
            if (param != null) {
                meta.getParams().add(param);
            }
        }
        return meta;
    }
}
