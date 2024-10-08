/*
 * Copyright (C) 2016 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.easyretrofit.adapter.reactor;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import reactor.core.publisher.Flux;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;

import java.io.IOException;

public final class FluxWithSchedulerTest {
  @Rule public final MockWebServer server = new MockWebServer();
  @Rule public final RecordingSubscriber.Rule subscriberRule = new RecordingSubscriber.Rule();
  private final long delay = 300;
  interface Service {
    @GET("/") Flux<String> body();
    @GET("/") Flux<Response<String>> response();
    @GET("/") Flux<Result<String>> result();
  }

  private final TestScheduler scheduler = new TestScheduler();
  private Service service;

  @Before public void setUp() {
    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(server.url("/"))
        .addConverterFactory(new StringConverterFactory())
        .addCallAdapterFactory(ReactorCallAdapterFactory.createWithScheduler(scheduler))
        .build();
    service = retrofit.create(Service.class);
  }

  @After
  public void tearDown() throws IOException {
    server.shutdown();
  }

  @Test public void bodyUsesScheduler() throws InterruptedException {
    server.enqueue(new MockResponse().setBody("Hi"));

    RecordingSubscriber<String> subscriber = subscriberRule.create();
    service.body().subscribe(subscriber);
    subscriber.assertNoEvents();
    scheduler.triggerActions();
    Thread.sleep(delay);
    subscriber.assertAnyValue().assertComplete();
  }

  @Test public void responseUsesScheduler() throws InterruptedException {
    server.enqueue(new MockResponse().setBody("Hi"));

    RecordingSubscriber<Response<String>> subscriber = subscriberRule.create();
    service.response().subscribe(subscriber);
    subscriber.assertNoEvents();

    scheduler.triggerActions();
    Thread.sleep(delay);
    subscriber.assertAnyValue().assertComplete();
  }

  @Test public void resultUsesScheduler() throws InterruptedException {
    server.enqueue(new MockResponse().setBody("Hi"));

    RecordingSubscriber<Result<String>> subscriber = subscriberRule.create();
    service.result().subscribe(subscriber);
    subscriber.assertNoEvents();

    scheduler.triggerActions();
    Thread.sleep(delay);
    subscriber.assertAnyValue().assertComplete();
  }
}
