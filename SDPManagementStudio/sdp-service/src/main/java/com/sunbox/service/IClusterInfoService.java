package com.sunbox.service;

import com.sunbox.domain.ResultMsg;
import com.sunbox.domain.cluster.TicketCreateRequest;

public interface IClusterInfoService {

      /**
       * 工单创建集群
       * @param jsonStr 原始json,前端传过来的
       * @return
       */
      ResultMsg createClusterByWorkOrderTicket (String jsonStr,String clusterId,String userName);


      /**
       * 工单销毁集群
       * @param jsonStr
       * @return
       */
      ResultMsg destroyClusterByWorkOrderTicket (String jsonStr,String clusterId,String userName);

      /**
       * 工单创建集群,发送请求
       * @param ticketCreateRequest
       * @param clusterId
       * @return
       */
      ResultMsg sendHttpRequest(TicketCreateRequest ticketCreateRequest, String clusterId,String requestType,String userName) ;

      /**
       * 判断下一步是否要调用销毁工单接口
       * @param clusterId
       * @return
       */
      @Deprecated
      Boolean checkIsDestroyWorkerOrder(String clusterId);

}
