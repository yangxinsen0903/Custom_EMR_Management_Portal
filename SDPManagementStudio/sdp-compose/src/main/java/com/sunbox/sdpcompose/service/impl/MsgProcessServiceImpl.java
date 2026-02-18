package com.sunbox.sdpcompose.service.impl;

import com.sunbox.domain.ResultMsg;
import com.sunbox.sdpcompose.producer.ProducerCache;
import com.sunbox.sdpcompose.service.IMsgProcessService;
import com.sunbox.sdpcompose.util.BeanMethod;
import com.sunbox.sdpcompose.util.SpringContextUtil;
import com.sunbox.web.BaseCommonInterFace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * @author : [niyang]
 * @className : MsgProcessServiceImpl
 * @description : [描述说明该类的功能]
 * @createTime : [2023/2/28 10:50 PM]
 */

@Service
public class MsgProcessServiceImpl implements IMsgProcessService, BaseCommonInterFace {


    @Autowired
    private SpringContextUtil app;

    /**
     * 传输处理
     *
     * @param callback
     * @param message
     */
    @Override
    @Async
    public void transforMessage(String callback, String message) {
        String classname=callback.split("@")[0];
        String methodname=callback.split("@")[1];

        BeanMethod beanmethod=null;
        Method method = null;
        if (ProducerCache.methods!=null
                && ProducerCache.methods.containsKey(classname)
                && ProducerCache.methods.get(classname).getMethods()!=null
                && ProducerCache.methods.get(classname).getMethods().containsKey(methodname)){
            beanmethod = ProducerCache.methods.get(classname);
            method=beanmethod.getMethods().get(methodname);
        }else{
            if (ProducerCache.methods==null){
                ProducerCache.methods=new HashMap<>();
            }
            Object obj= app.getBean(classname);
            Class cls=obj.getClass();
            Class[] classes=new Class[1];
            classes[0]= String.class ;
            beanmethod=new BeanMethod();
            beanmethod.setObj(obj);
            try {
                method=cls.getMethod(methodname,classes);
                HashMap<String,Method> methodHashMap=beanmethod.getMethods();
                if (methodHashMap==null){
                    methodHashMap=new HashMap<>();
                }
                methodHashMap.put(methodname,method);
                beanmethod.setMethods(methodHashMap);
                ProducerCache.methods.put(callback,beanmethod);
            } catch (Exception e) {
                getLogger().error("reflact exception：",e);
            }
        }
        Object[] params=new Object[1];
        params[0]= message;
        try {
            method.invoke(beanmethod.getObj(), params);
        }catch (Exception e){
            getLogger().error("call back exception-"+callback+"：",e);
        }
    }
}
