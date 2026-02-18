package com.sunbox.sdpcompose.service.ambari;

import java.util.List;

/**
 * 删除主机结果
 * @author: wangda
 * @date: 2023/1/18
 */
public class DeleteHostResult {

    /** 是否全部删除 */
    private boolean isAllDeleted;

    /** 成功删除的主机列表 */
    private List<String> deleteSuccessHosts;

    /** 成功删除的组件 */
    private List<String> deleteSuccessComponents;

    /** 删除失败的主机列表 */
    private List<String> deleteFailHosts;

    /** 删除失败的主机列表 */
    private List<String> deleteFailComponents;

    public boolean isAllDeleted() {
        return isAllDeleted;
    }

    public void setAllDeleted(boolean allDeleted) {
        isAllDeleted = allDeleted;
    }

    public List<String> getDeleteSuccessHosts() {
        return deleteSuccessHosts;
    }

    public void setDeleteSuccessHosts(List<String> deleteSuccessHosts) {
        this.deleteSuccessHosts = deleteSuccessHosts;
    }

    public List<String> getDeleteFailHosts() {
        return deleteFailHosts;
    }

    public void setDeleteFailHosts(List<String> deleteFailHosts) {
        this.deleteFailHosts = deleteFailHosts;
    }

    public List<String> getDeleteSuccessComponents() {
        return deleteSuccessComponents;
    }

    public void setDeleteSuccessComponents(List<String> deleteSuccessComponents) {
        this.deleteSuccessComponents = deleteSuccessComponents;
    }

    public List<String> getDeleteFailComponents() {
        return deleteFailComponents;
    }

    public void setDeleteFailComponents(List<String> deleteFailComponents) {
        this.deleteFailComponents = deleteFailComponents;
    }
}
