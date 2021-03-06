package org.kafkahq.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.config.internals.BrokerSecurityConfigs;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ToString
@EqualsAndHashCode
@Getter
public class Config {
    private final String name;
    private final String value;
    private final String description;
    private final Source source;
    private final boolean isSensitive;
    private final boolean isReadOnly;
    private final List<Synonym> synonyms = new ArrayList<>();

    public Config(ConfigEntry entry) {
        this.name = entry.name();
        this.value = entry.value();
        this.description = findDescription(this.name);
        this.source = Source.valueOf(entry.source().name());
        this.isSensitive = entry.isSensitive();
        this.isReadOnly = entry.isReadOnly();

        for (ConfigEntry.ConfigSynonym item: entry.synonyms()) {
            this.synonyms.add(new Synonym(item));
        }
    }

    private String findDescription(String name) {
        String docName = name.toUpperCase().replace(".", "_") + "_DOC";

        List<Class<?>> classes = Arrays.asList(
            TopicConfig.class,
            BrokerSecurityConfigs.class,
            SslConfigs.class,
            SaslConfigs.class
        );

        for(Class<?> cls : classes) {
            try {
                Field declaredField = cls.getDeclaredField(docName);
                return declaredField.get(cls.newInstance()).toString();
            } catch (NoSuchFieldException | IllegalAccessException | InstantiationException e) { }
        }

        return null;
    }

    @ToString
    @EqualsAndHashCode
    @Getter
    public static class Synonym {
        private final String name;
        private final String value;
        private final Source source;

        public Synonym(ConfigEntry.ConfigSynonym synonym) {
            this.name = synonym.name();
            this.value = synonym.value();
            this.source = Source.valueOf(synonym.source().name());
        }
    }

    public enum Source {
        DYNAMIC_TOPIC_CONFIG,
        DYNAMIC_BROKER_CONFIG,
        DYNAMIC_DEFAULT_BROKER_CONFIG,
        STATIC_BROKER_CONFIG,
        DEFAULT_CONFIG,
        UNKNOWN
    }
}
