package com.azure.csu.tiger.ansible.api.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.jooq.TableOptions;

import java.io.IOException;

public class TableOptionsSerializer extends StdSerializer {

    public TableOptionsSerializer() {
        super(TableOptions.class);
    }

//    @Override
//    public void serialize(TableOptions value, JsonGenerator gen, SerializerProvider provider) throws IOException {
//        gen.writeStartObject();
//        gen.writeStringField("type", value.type().name());
//        gen.writeEndObject();
//    }

    @Override
    public void serialize(Object value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        TableOptions tValue = (TableOptions)value;
        jsonGenerator.writeStringField("type", tValue.type().name());
        jsonGenerator.writeEndObject();
    }
}

