package ru.webotix.datasource.database;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.alfasoftware.morf.jdbc.DatabaseType;

import java.net.URI;
import java.util.Iterator;
import java.util.Optional;

public class ConnectionResourceFactory {

    public Optional<UrlConnectionResourcesBean> build(DatabaseType databaseType, String url) {

        Iterator<String> split = Splitter.on(':').split(url).iterator();

        if (!"jdbc".equals(split.next())) {
            return Optional.empty();
        }
        if (!"h2".equals(split.next())) {
            return Optional.empty();
        }

        String databaseName;

        String protocol = split.next();
        switch (protocol) {
            case "mem":
            case "file":
                databaseName = Joiner.on(':').join(split);
                break;
            case "tcp":
                URI theRest = URI.create("tcp:" + Joiner.on(':').join(split));
                if (!theRest.getPath().startsWith("mem:")) {
                    return Optional.empty();
                }
                databaseName = theRest.getPath().substring(4);
                break;
            default: return Optional.empty();
        }

        return Optional.of(new UrlConnectionResourcesBean(url, databaseType.identifier(), databaseName));
    }
}
