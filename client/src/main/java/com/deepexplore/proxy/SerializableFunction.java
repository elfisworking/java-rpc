package com.deepexplore.proxy;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
// https://www.cnblogs.com/throwable/p/15611586.html
// 函数式接口序列化
public interface SerializableFunction<T> extends Serializable {
    default String getName() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = this.getClass().getDeclaredMethod("writeReplace");
        method.setAccessible(true);
        SerializedLambda serializedLambda = (SerializedLambda)method.invoke(this);
        return serializedLambda.getImplMethodName();
    }
}
