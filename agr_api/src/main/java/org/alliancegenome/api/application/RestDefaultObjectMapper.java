package org.alliancegenome.api.application;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.alliancegenome.api.service.helper.RangeSerializer;
import org.alliancegenome.core.config.ConfigHelper;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.lang3.Range;

@Provider
public class RestDefaultObjectMapper implements ContextResolver<ObjectMapper> {

    private final ObjectMapper mapper;

    public RestDefaultObjectMapper() {
        mapper = new ObjectMapper();
        mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        if (!ConfigHelper.isProduction())
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
//        mapper.getSerializerProvider().setNullValueSerializer(new NullSerializer());
        SimpleModule module = new SimpleModule();
        module.addSerializer(Range.class, new RangeSerializer());
        mapper.registerModule(module);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

}

/*
class NullSerializer extends JsonSerializer<Object> {

    @Override
    public void serialize(Object o, com.fasterxml.jackson.core.JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, com.fasterxml.jackson.core.JsonProcessingException {
        jsonGenerator.writeString("");
    }
}*/
