package org.springframework.batch.item.redisearch;

import com.redislabs.lettuce.helper.RedisOptions;
import com.redislabs.lettusearch.StatefulRediSearchConnection;
import com.redislabs.lettusearch.aggregate.AggregateOptions;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.batch.item.redisearch.support.LettuSearchHelper;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.util.Iterator;
import java.util.Map;

public class RediSearchAggregateItemReader<K, V> extends AbstractItemCountingItemStreamItemReader<Map<K, V>> {

    private final StatefulRediSearchConnection<K, V> connection;
    private final K index;
    private final V query;
    private final AggregateOptions aggregateOptions;

    private Iterator<Map<K, V>> results;

    public RediSearchAggregateItemReader(StatefulRediSearchConnection<K, V> connection, K index, V query, AggregateOptions aggregateOptions) {
        setName(ClassUtils.getShortName(getClass()));
        Assert.notNull(connection, "A RediSearch connection is required.");
        Assert.notNull(index, "An index name is required.");
        Assert.notNull(query, "A query is required.");
        this.connection = connection;
        this.index = index;
        this.query = query;
        this.aggregateOptions = aggregateOptions;
    }

    @Override
    protected void doOpen() {
        this.results = connection.sync().aggregate(index, query, aggregateOptions).iterator();
    }

    @Override
    protected Map<K, V> doRead() {
        if (results.hasNext()) {
            return results.next();
        }
        return null;
    }

    @Override
    protected void doClose() {
        this.results = null;
    }

    public static RediSearchAggregateItemReaderBuilder builder() {
        return new RediSearchAggregateItemReaderBuilder();
    }

    @Setter
    @Accessors(fluent = true)
    public static class RediSearchAggregateItemReaderBuilder {

        private RedisOptions redisOptions;
        private String index;
        private String query;
        private AggregateOptions aggregateOptions;

        public RediSearchAggregateItemReader<String, String> build() {
            Assert.notNull(redisOptions, "Redis options are required");
            return new RediSearchAggregateItemReader<>(LettuSearchHelper.connection(redisOptions), index, query, aggregateOptions);
        }
    }

}
