/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.search.aggregations.pipeline;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.DocValueFormat;
import org.elasticsearch.search.aggregations.AggregationStreams;
import org.elasticsearch.search.aggregations.InternalAggregation;
import org.elasticsearch.search.aggregations.metrics.InternalNumericMetricsAggregation;
import org.elasticsearch.search.aggregations.metrics.max.InternalMax;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class InternalSimpleValue extends InternalNumericMetricsAggregation.SingleValue implements SimpleValue {

    public static final Type TYPE = new Type("simple_value");

    public static final AggregationStreams.Stream STREAM = new AggregationStreams.Stream() {
        @Override
        public InternalSimpleValue readResult(StreamInput in) throws IOException {
            InternalSimpleValue result = new InternalSimpleValue();
            result.readFrom(in);
            return result;
        }
    };

    public static void registerStreams() {
        AggregationStreams.registerStream(STREAM, TYPE.stream());
    }

    private double value;

    protected InternalSimpleValue() { // NORELEASE remove and make value final if possible
    } // for serialization

    public InternalSimpleValue(String name, double value, DocValueFormat formatter, List<PipelineAggregator> pipelineAggregators,
            Map<String, Object> metaData) {
        super(name, pipelineAggregators, metaData);
        this.format = formatter;
        this.value = value;
    }

    /**
     * Read from a stream.
     */
    public InternalSimpleValue(StreamInput in) throws IOException {
        super(in);
        format = in.readNamedWriteable(DocValueFormat.class);
        value = in.readDouble();
    }

    @Override
    protected void doReadFrom(StreamInput in) throws IOException {
        format = in.readNamedWriteable(DocValueFormat.class);
        value = in.readDouble();
    }

    @Override
    protected void doWriteTo(StreamOutput out) throws IOException {
        out.writeNamedWriteable(format);
        out.writeDouble(value);
    }

    @Override
    public double value() {
        return value;
    }

    public double getValue() {
        return value;
    }

    @Override
    public Type type() {
        return TYPE;
    }

    @Override
    public InternalMax doReduce(List<InternalAggregation> aggregations, ReduceContext reduceContext) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public XContentBuilder doXContentBody(XContentBuilder builder, Params params) throws IOException {
        boolean hasValue = !(Double.isInfinite(value) || Double.isNaN(value));
        builder.field(CommonFields.VALUE, hasValue ? value : null);
        if (hasValue && format != DocValueFormat.RAW) {
            builder.field(CommonFields.VALUE_AS_STRING, format.format(value));
        }
        return builder;
    }
}
