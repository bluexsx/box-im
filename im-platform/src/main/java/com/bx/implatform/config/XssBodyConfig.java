package com.bx.implatform.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Configuration
public class XssBodyConfig {

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(String.class, new JsonHtmlXssDeserializer());
        objectMapper.registerModule(simpleModule);
    }


    class JsonHtmlXssDeserializer extends JsonDeserializer {

        @Override
        public Class<String> handledType() {
            return String.class;
        }

        @Override
        public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            String value = jsonParser.getValueAsString();
            if (StringUtils.isNotEmpty(value)) {
                return StringEscapeUtils.escapeHtml4(value);
            }
            return value;
        }

}
}