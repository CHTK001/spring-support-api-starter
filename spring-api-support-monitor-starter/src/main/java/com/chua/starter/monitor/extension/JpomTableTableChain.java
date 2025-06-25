//package com.chua.starter.monitor.extension;
//
//import com.chua.common.support.collection.AnnotationAttributes;
//import com.chua.common.support.datasource.resolver.chain.TableChain;
//import com.chua.starter.monitor.jpom.db.TableName;
//
//import java.lang.annotation.Annotation;
//import java.util.concurrent.atomic.AtomicReference;
//
///**
// * JpomTableTableChain
// * @author CH
// * @since 2025/6/6 11:06
// */
//public class JpomTableTableChain implements TableChain{
//    @Override
//    public void chain(AtomicReference<String> atomicReference, Class<?> aClass, AnnotationAttributes annotationAttributes) {
//        String value = annotationAttributes.getString("value");
//        if(null != value) {
//            atomicReference.set(value);
//        }
//    }
//
//    @Override
//    public Class<? extends Annotation> annotationType() {
//        return TableName.class;
//    }
//}
