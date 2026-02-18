package com.sunbox.sdpcompose.util;

import java.lang.reflect.Method;
import java.util.HashMap;

public class BeanMethod {

    private Object obj;

    private HashMap<String,Method> methods;


    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public HashMap<String, Method> getMethods() {
        return methods;
    }

    public void setMethods(HashMap<String, Method> methods) {
        this.methods = methods;
    }
}
