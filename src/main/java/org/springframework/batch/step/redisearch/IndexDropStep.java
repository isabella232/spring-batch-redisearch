package org.springframework.batch.step.redisearch;

import com.redislabs.lettusearch.StatefulRediSearchConnection;
import com.redislabs.lettusearch.index.DropOptions;
import io.lettuce.core.RedisCommandExecutionException;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.step.AbstractStep;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

@Slf4j
public class IndexDropStep<K, V> extends AbstractStep {

    private final StatefulRediSearchConnection<K, V> connection;
    private final K index;
    private final DropOptions dropOptions;
    private final boolean ignoreErrors;

    @Builder
    public IndexDropStep(StatefulRediSearchConnection<K, V> connection, K index, DropOptions dropOptions, boolean ignoreErrors) {
        setName(ClassUtils.getShortName(getClass()));
        Assert.notNull(connection, "A RediSearch connection is required.");
        Assert.notNull(index, "An index name is required.");
        this.connection = connection;
        this.index = index;
        this.dropOptions = dropOptions;
        this.ignoreErrors = ignoreErrors;
    }

    @Override
    protected void doExecute(StepExecution stepExecution) {
        try {
            connection.sync().drop(index, dropOptions);
        } catch (RedisCommandExecutionException e) {
            if (ignoreErrors) {
                log.debug("Could not drop index {}", index, e);
            } else {
                throw e;
            }
        }
    }

}
