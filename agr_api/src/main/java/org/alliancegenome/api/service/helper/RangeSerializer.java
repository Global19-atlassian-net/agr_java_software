package org.alliancegenome.api.service.helper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.apache.commons.lang3.Range;

import java.io.IOException;

public class RangeSerializer extends StdSerializer<Range> {

    public RangeSerializer() {
        this(null);
    }

    public RangeSerializer(Class<Range> t) {
        super(t);
    }

    @Override
    public void serialize(Range range, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("range", range.getMinimum().toString()+" - "+ range.getMaximum().toString());
        jsonGenerator.writeStringField("min", range.getMinimum().toString());
        jsonGenerator.writeStringField("max", range.getMaximum().toString());
        jsonGenerator.writeEndObject();
    }
}
