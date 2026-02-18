package sunbox.sdp.ambari.client.model.customaction.enums;

/**
 * 配置组字段，用来约束查询条件
 * @author: wangda
 * @date: 2023/2/11
 */
public enum ConfigGroupField {
    /** 配置组的Tag，Ambari默认会将Service设置到Tag里 */
    TAG("tag"),
    /** 配置组名，创建配置组时自己指定的 */
    GROUP_NAME("group_name"),
    /** 配置组的ID，创建配置组完成后， Ambari返回的ID值 */
    ID("id");

    /** 配置组查询时的字段名 */
    private String fieldName;

    ConfigGroupField(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
