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

package grakn.client.rpc;

import grakn.client.Grakn.Client;
import grakn.client.Grakn.DatabaseManager;
import grakn.client.Grakn.Session;
import grakn.client.GraknOptions;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.TimeUnit;

public class GraknClient implements Client {

    public static final String DEFAULT_URI = "localhost:1729";

    private final ManagedChannel channel;
    private final DatabaseManager databases;

    public GraknClient() {
        this(DEFAULT_URI);
    }

    public GraknClient(String address) {
        channel = ManagedChannelBuilder.forTarget(address).usePlaintext().build();
        databases = new RPCDatabaseManager(channel);
    }

    @Override
    public Session session(String database, Session.Type type) {
        return session(database, type, new GraknOptions());
    }

    @Override
    public Session session(String database, Session.Type type, GraknOptions options) {
        return new RPCSession(this, database, type, options);
    }

    @Override
    public DatabaseManager databases() {
        return databases;
    }

    @Override
    public boolean isOpen() {
        return !channel.isShutdown();
    }

    @Override
    public void close() {
        try {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    Channel channel() {
        return channel;
    }
}
