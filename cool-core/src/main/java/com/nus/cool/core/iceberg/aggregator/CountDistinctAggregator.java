/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.nus.cool.core.iceberg.aggregator;

import com.nus.cool.core.schema.FieldType;
import com.nus.cool.core.iceberg.result.AggregatorResult;
import com.nus.cool.core.io.storevector.InputVector;
import com.nus.cool.core.io.readstore.FieldRS;
import com.nus.cool.core.io.readstore.MetaFieldRS;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class CountDistinctAggregator implements Aggregator{
    @Override
    public void process(Map<String, BitSet> groups, FieldRS field,
                        Map<String, AggregatorResult> resultMap, MetaFieldRS metaFieldRS) {
        for (Map.Entry<String, BitSet> entry : groups.entrySet()) {
            if (resultMap.get(entry.getKey()) == null) {
                String groupNmae = entry.getKey();
                AggregatorResult aggregatorResult = new AggregatorResult();
                resultMap.put(groupNmae, aggregatorResult);
            }
            AggregatorResult result = resultMap.get(entry.getKey());
            if (result.getCountDistinct() == null) {
                if (field.getFieldType() == FieldType.Metric) {
                    throw new UnsupportedOperationException();
                }
                BitSet bs = entry.getValue();
                InputVector key = field.getKeyVector();
                InputVector value = field.getValueVector();
                Set<Integer> set = new HashSet<>();
                for (int i = 0; i < bs.size(); i++) {
                    int nextPos = bs.nextSetBit(i);
                    if (nextPos < 0) {
                        break;
                    }
                    set.add(value.get(nextPos));
                    i = nextPos;
                }
                for (Integer i : set) {
                    result.getDistinctSet().add(metaFieldRS.getString(key.get(i)));
                }

                result.setCountDistinct((float) result.getDistinctSet().size());
            }
        }
    }
}
