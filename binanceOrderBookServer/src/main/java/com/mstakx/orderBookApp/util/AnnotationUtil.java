package com.mstakx.orderBookApp.util;

import com.mstakx.orderBookApp.dto.DynamicMeasurement;
import org.influxdb.annotation.Measurement;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public class AnnotationUtil {

    private static final String ANNOTATION_METHOD = "annotationData";
    private static final String ANNOTATIONS = "annotations";

    public static void alterAnnotationValue(String measurementName, Class className) {
        Measurement updatedMeasurement = new DynamicMeasurement(measurementName);
        alterAnnotationValueJDK8(className, Measurement.class, updatedMeasurement);
    }


    @SuppressWarnings("unchecked")
    public static void alterAnnotationValueJDK8(Class<?> targetClass, Class<? extends Annotation> targetAnnotation, Annotation targetValue) {
        try {
            Method method = Class.class.getDeclaredMethod(ANNOTATION_METHOD, null);
            method.setAccessible(true);

            Object annotationData = method.invoke(targetClass);

            Field annotations = annotationData.getClass().getDeclaredField(ANNOTATIONS);
            annotations.setAccessible(true);

            Map<Class<? extends Annotation>, Annotation> map = (Map<Class<? extends Annotation>, Annotation>) annotations.get(annotationData);
            map.put(targetAnnotation, targetValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
