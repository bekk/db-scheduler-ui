/*
 * Copyright (C) Marten Prieß
 * Originally part of https://github.com/rocketbase-io/db-scheduler-log
 * Copyright (C) Bekk (modifications)
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.bekk.dbscheduler.ui.log.jdbc;

import com.github.kagkarlsson.scheduler.SchedulerName;
import java.security.SecureRandom;
import java.time.Instant;

/**
 * Distributed Sequence Generator. Inspired by Twitter snowflake:
 * https://github.com/twitter/snowflake/tree/snowflake-2010
 *
 * <p>Should be used as a singleton. Make sure that you create and reuse a single instance per node
 * in your distributed system cluster.
 *
 * <p>Adapted from java-snowflake by callicoder (https://github.com/callicoder/java-snowflake).
 */
public final class Snowflake implements IdProvider {

  private static Snowflake instance;

  public static final int UNUSED_BITS = 1; // Sign bit, unused (always set to 0)
  public static final int EPOCH_BITS = 43;
  public static final int NODE_ID_BITS = 10;
  public static final int SEQUENCE_BITS = 10;

  public static final long maxNodeId = (1L << NODE_ID_BITS) - 1;
  public static final long maxSequence = (1L << SEQUENCE_BITS) - 1;

  // Custom Epoch (January 1, 2020 Midnight UTC = 2020-01-01T00:00:00Z)
  public static final long DEFAULT_CUSTOM_EPOCH = 1577836800000L;

  private final long nodeId;
  private final long customEpoch;

  private volatile long lastTimestamp = -1L;
  private volatile long sequence = 0L;

  public Snowflake(long nodeId, long customEpoch) {
    if (nodeId < 0 || nodeId > maxNodeId) {
      throw new IllegalArgumentException(
          String.format("NodeId must be between %d and %d", 0, maxNodeId));
    }
    this.nodeId = nodeId;
    this.customEpoch = customEpoch;
  }

  public Snowflake(long nodeId) {
    this(nodeId, DEFAULT_CUSTOM_EPOCH);
  }

  public Snowflake() {
    this.nodeId = createNodeId();
    this.customEpoch = DEFAULT_CUSTOM_EPOCH;
  }

  public static synchronized Snowflake getInstance() {
    if (instance == null) {
      instance = new Snowflake();
    }
    return instance;
  }

  @Override
  public synchronized long nextId() {
    long currentTimestamp = timestamp();

    if (currentTimestamp < lastTimestamp) {
      throw new IllegalStateException("Invalid System Clock!");
    }

    if (currentTimestamp == lastTimestamp) {
      sequence = (sequence + 1) & maxSequence;
      if (sequence == 0) {
        // Sequence exhausted, wait till next millisecond.
        currentTimestamp = waitNextMillis(currentTimestamp);
      }
    } else {
      // reset sequence to start with zero for the next millisecond
      sequence = 0;
    }

    lastTimestamp = currentTimestamp;

    return currentTimestamp << (NODE_ID_BITS + SEQUENCE_BITS)
        | (nodeId << SEQUENCE_BITS)
        | sequence;
  }

  private long timestamp() {
    return Instant.now().toEpochMilli() - customEpoch;
  }

  private long waitNextMillis(long currentTimestamp) {
    while (currentTimestamp == lastTimestamp) {
      currentTimestamp = timestamp();
    }
    return currentTimestamp;
  }

  private long createNodeId() {
    long nodeId;
    try {
      nodeId = new SchedulerName.Hostname().hashCode();
    } catch (Exception ex) {
      nodeId = new SecureRandom().nextInt();
    }
    return nodeId & maxNodeId;
  }

  public long[] parse(long id) {
    long maskNodeId = ((1L << NODE_ID_BITS) - 1) << SEQUENCE_BITS;
    long maskSequence = (1L << SEQUENCE_BITS) - 1;

    long timestamp = (id >> (NODE_ID_BITS + SEQUENCE_BITS)) + customEpoch;
    long parsedNodeId = (id & maskNodeId) >> SEQUENCE_BITS;
    long parsedSequence = id & maskSequence;

    return new long[] {timestamp, parsedNodeId, parsedSequence};
  }

  @Override
  public String toString() {
    return "Snowflake Settings [EPOCH_BITS="
        + EPOCH_BITS
        + ", NODE_ID_BITS="
        + NODE_ID_BITS
        + ", SEQUENCE_BITS="
        + SEQUENCE_BITS
        + ", CUSTOM_EPOCH="
        + customEpoch
        + ", NodeId="
        + nodeId
        + "]";
  }
}
