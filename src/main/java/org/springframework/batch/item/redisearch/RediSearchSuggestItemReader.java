package org.springframework.batch.item.redisearch;

import com.redislabs.lettusearch.StatefulRediSearchConnection;
import com.redislabs.lettusearch.suggest.Suggestion;
import com.redislabs.lettusearch.suggest.SuggetOptions;
import lombok.Builder;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.util.Iterator;

public class RediSearchSuggestItemReader<K, V> extends AbstractItemCountingItemStreamItemReader<Suggestion<V>> {

    private final StatefulRediSearchConnection<K, V> connection;
    private final K key;
    private final V prefix;
    private final SuggetOptions suggetOptions;

    private Iterator<Suggestion<V>> results;

    @Builder
    public RediSearchSuggestItemReader(StatefulRediSearchConnection<K, V> connection, K key, V prefix, SuggetOptions suggetOptions) {
        setName(ClassUtils.getShortName(getClass()));
        Assert.notNull(connection, "A RediSearch connection is required.");
        Assert.notNull(key, "A key is required.");
        Assert.notNull(prefix, "A prefix is required.");
        this.connection = connection;
        this.key = key;
        this.prefix = prefix;
        this.suggetOptions = suggetOptions;
    }

    @Override
    protected void doOpen() {
        this.results = connection.sync().sugget(key, prefix, suggetOptions).iterator();
    }

    @Override
    protected Suggestion<V> doRead() {
        if (results.hasNext()) {
            return results.next();
        }
        return null;
    }

    @Override
    protected void doClose() {
        this.results = null;
    }


}
