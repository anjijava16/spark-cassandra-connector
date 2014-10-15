package com.datastax.spark.connector.rdd;

import com.datastax.spark.connector.cql.CassandraConnector;
import com.datastax.spark.connector.util.JavaApiHelper;
import org.apache.spark.api.java.JavaPairRDD;
import scala.Tuple2;
import scala.reflect.ClassTag;

import static com.datastax.spark.connector.util.JavaApiHelper.getClassTag;
import static com.datastax.spark.connector.util.JavaApiHelper.toScalaSeq;

/**
 * A Java wrapper for {@link CassandraRDD}. Makes the invocation of
 * Cassandra related methods easy in Java.
 */
public class CassandraJavaPairRDD<K, V> extends JavaPairRDD<K, V> {
    public CassandraJavaPairRDD(CassandraRDD<Tuple2<K, V>> rdd, Class<K> keyClass, Class<V> valueClass) {
        super(rdd, getClassTag(keyClass), getClassTag(valueClass));
    }

    public CassandraJavaPairRDD(CassandraRDD<Tuple2<K, V>> rdd, ClassTag<K> keyClassTag, ClassTag<V> valueClassTag) {
        super(rdd, keyClassTag, valueClassTag);
    }

    @Override
    public CassandraRDD<Tuple2<K, V>> rdd() {
        return (CassandraRDD<Tuple2<K, V>>) super.rdd();
    }

    /**
     * Narrows down the selected set of columns.
     * Use this for better performance, when you don't need all the columns in the result RDD.
     * When called multiple times, it selects the subset of the already selected columns, so
     * after a column was removed by the previous {@code select} call, it is not possible to
     * add it back.
     */
    public CassandraJavaPairRDD<K, V> select(String... columnNames) {
        // explicit type argument is intentional and required here
        //noinspection RedundantTypeArguments
        CassandraRDD<Tuple2<K, V>> newRDD = rdd().select(JavaApiHelper.<String>toScalaSeq(columnNames));
        return new CassandraJavaPairRDD<>(newRDD, kClassTag(), vClassTag());
    }

    /**
     * Adds a CQL {@code WHERE} predicate(s) to the query.
     * Useful for leveraging secondary indexes in Cassandra.
     * Implicitly adds an {@code ALLOW FILTERING} clause to the {@code WHERE} clause, however beware that some predicates
     * might be rejected by Cassandra, particularly in cases when they filter on an unindexed, non-clustering column.
     */
    public CassandraJavaPairRDD<K, V> where(String cqlWhereClause, Object... args) {
        CassandraRDD<Tuple2<K, V>> newRDD = rdd().where(cqlWhereClause, toScalaSeq(args));
        return new CassandraJavaPairRDD<>(newRDD, kClassTag(), vClassTag());
    }

    /**
     * Returns the names of columns to be selected from the table.
     */
    public String[] selectedColumnNames() {
        // explicit type cast is intentional and required here
        //noinspection RedundantCast
        return (String[]) rdd().selectedColumnNames().<String>toArray(getClassTag(String.class));
    }

    /**
     * Returns a copy of this RDD with connector changed to the specified one.
     */
    public CassandraJavaPairRDD<K, V> withCassandraConnector(CassandraConnector connector) {
        CassandraRDD<Tuple2<K, V>> newRDD = rdd().withConnector(connector);
        return new CassandraJavaPairRDD<>(newRDD, kClassTag(), vClassTag());
    }

    /**
     * Returns a copy of this RDD with read configuration changed to the specified one.
     */
    public CassandraJavaPairRDD<K, V> withReadConf(ReadConf config) {
        CassandraRDD<Tuple2<K, V>> newRDD = rdd().withReadConf(config);
        return new CassandraJavaPairRDD<>(newRDD, kClassTag(), vClassTag());
    }

}
