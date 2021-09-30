package org.lmmarise.netty.rpc.protocol;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author lmmarise.j@gmail.com
 * @since 2021/9/29 10:05 下午
 */
public class InvokerProtocol implements Serializable {
    private String className;       // 类名
    private String methodName;      // 函数名称
    private Class<?>[] params;      // 形参列表
    private Object[] values;        // 实参列表

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParams() {
        return params;
    }

    public void setParams(Class<?>[] params) {
        this.params = params;
    }

    public Object[] getValues() {
        return values;
    }

    public void setValues(Object[] values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "InvokerProtocol{" +
                "className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", params=" + Arrays.toString(params) +
                ", values=" + Arrays.toString(values) +
                '}';
    }
}
