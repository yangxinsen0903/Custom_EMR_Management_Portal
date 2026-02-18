package sunbox.sdp.ambari.client.model.customaction;

import cn.hutool.core.convert.Convert;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 删除主机请求的响应对象
 * @author: wangda
 * @date: 2023/1/12
 */
public class DeleteHostsResponse {

    @SerializedName("deleteResult")
    private List<Map> deleteResult;

    /**
     * 是否全部删除，所有主机都明确的删除成功。
     * @return true: 删除成功   false: 删除失败
     */
    public boolean isAllDeleted() {
        if (Objects.isNull(deleteResult)) {
            return false;
        }
        for (Map map : deleteResult) {
            Object error = map.get("error");
            if (Objects.nonNull(error)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 是否删除成功，删除成功的条件为：所有主机不是被删除，就是主机不存在。<br/>
     * 当此方法返回true，认为所有主机都已在Ambari上删除，可以进行下一步操作。
     * @return true: 删除成功   false: 删除失败
     */
    public boolean isDeleteSuccess() {
        if (Objects.isNull(deleteResult)) {
            return false;
        }
        for (Map map : deleteResult) {
            if (Objects.nonNull(map.get("deleted"))) {
                // 有deleted节点，说明删除成功
                continue;
            }

            Integer code = getErrorMapCode(map, 0);
            if (!Objects.equals(code, 404)) {
                return false;
            }
        }

        return true;
    }

    public List<String> get404HostList() {
        return getFailList(404);
    }

    /**
     * 获取删除成功的主机或组件
     * @return
     */
    public List<String> getSuccessHostList() {
        List<String> succeedList = new ArrayList<>();
        if (Objects.isNull(deleteResult)) {
            return succeedList;
        }
        for (Map map : deleteResult) {
            Map<String, Object> deleted = (Map<String, Object>) map.get("deleted");
            if (Objects.nonNull(deleted)) {
                String deleteElement = String.valueOf(deleted.get("key"));
                if (deleteElement.indexOf("/") == -1) {
                    succeedList.add(String.valueOf(deleted.get("key")));
                }
            }
        }
        return succeedList;
    }

    public List<String> getSuccessComponentList() {
        List<String> succeedComponentList = new ArrayList<>();
        if (Objects.isNull(deleteResult)) {
            return succeedComponentList;
        }
        for (Map map : deleteResult) {
            Map<String, Object> deleted = (Map<String, Object>) map.get("deleted");
            if (Objects.nonNull(deleted)) {
                String deleteElement = String.valueOf(deleted.get("key"));
                if (deleteElement.indexOf("/") >= 0) {
                    succeedComponentList.add(String.valueOf(deleted.get("key")));
                }
            }
        }
        return succeedComponentList;
    }

    public List<String> get500FailList() {
        return getFailList(500);
    }

    public List<String> getFailList(Integer errorCode) {
        List<String> failList = new ArrayList<>();
        if (Objects.isNull(deleteResult)) {
            return failList;
        }

        for (Map map : deleteResult) {
            Integer code = getErrorMapCode(map, 0);
            Map<String, Object> errorMap = getErrorMap(map);

            if (Objects.equals(code, errorCode)) {
                // 不是deleted时，就是error
                failList.add(String.valueOf(errorMap.get("key")));
            }
        }
        return failList;
    }

    private Map<String, Object> getErrorMap(Map map) {
        if (Objects.isNull(map)) {
            return null;
        }

        Object error = map.get("error");
        if (Objects.isNull(error)) {
            return null;
        }

        return error instanceof Map? (Map)error: null;
    }

    /**
     * 从Error节点中取Code的值
     * @param map
     * @param def
     * @return
     */
    private Integer getErrorMapCode(Map map, Integer def) {
        Map<String, Object> errorMap = getErrorMap(map);
        if (Objects.nonNull(errorMap)) {
            return Convert.toInt(errorMap.get("code"), def);
        } else {
            return def;
        }
    }


    public List<Map> getDeleteResult() {
        return deleteResult;
    }

    public void setDeleteResult(List<Map> deleteResult) {
        this.deleteResult = deleteResult;
    }
}
