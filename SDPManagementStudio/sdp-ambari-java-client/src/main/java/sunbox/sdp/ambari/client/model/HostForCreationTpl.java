package sunbox.sdp.ambari.client.model;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class HostForCreationTpl {

    /** 主机名 */
    @SerializedName("fqdn")
    private String fqdn;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HostForCreationTpl hostGroupInfo = (HostForCreationTpl) o;
        return Objects.equals(this.fqdn, hostGroupInfo.fqdn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fqdn);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class HostForCreationTpl {\n");

        sb.append("    fqdn: ").append(toIndentedString(fqdn)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
