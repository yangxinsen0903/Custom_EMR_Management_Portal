package com.sunbox.sdpcompose.util;


import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

/**
 * @author : [niyang]
 * @className : MyRepresenter
 * @description : [描述说明该类的功能]
 * @createTime : [2023/3/8 11:10 PM]
 */
public class MyRepresenter extends Representer {
    public MyRepresenter(){
        this.representers.put(QuotedString.class,new RepresenterQuotedString());
    }
    public class RepresenterQuotedString implements Represent {
        @Override
        public Node representData(Object o) {
            if (o instanceof QuotedString) {
                QuotedString str = (QuotedString) o;
                return representScalar(Tag.STR, str.value, DumperOptions.ScalarStyle.DOUBLE_QUOTED);
            }
            return null;
        }
    }
}
