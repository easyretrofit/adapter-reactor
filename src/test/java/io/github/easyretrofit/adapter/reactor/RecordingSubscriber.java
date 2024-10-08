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

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Signal;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

/** A test {@link Subscriber} and JUnit rule which guarantees all events are asserted. */
final class RecordingSubscriber<T> implements Subscriber<T> {
  private final long initialRequest;
  private final Deque<Signal<T>> events = new ArrayDeque<>();

  private Subscription s;

  private RecordingSubscriber(long initialRequest) {
    this.initialRequest = initialRequest;
  }

  @Override public void onSubscribe(Subscription s) {
    this.s = s;
    s.request(initialRequest);
  }

  @Override public void onNext(T value) {
    events.add(Signal.next(value));
  }

  @Override public void onComplete() {
    events.add(Signal.<T>complete());
  }

  @Override public void onError(Throwable e) {
    events.add(Signal.<T>error(e));
  }

  private Signal<T> takeSignal() {
    Signal<T> signal = events.pollFirst();
    if (signal == null) {
      throw new AssertionError("No event found!");
    }
    return signal;
  }

  public T takeValue() {
    Signal<T> signal = takeSignal();
    assertThat(signal.isOnNext())
        .named("Expected onNext event but was %s", signal)
        .isTrue();
    return signal.get();
  }

  public Throwable takeError() {
    Signal<T> signal = takeSignal();
    assertThat(signal.isOnError())
        .named("Expected onError event but was %s", signal)
        .isTrue();
    return signal.getThrowable();
  }

  public RecordingSubscriber<T> assertAnyValue() {
    takeValue();
    return this;
  }

  public RecordingSubscriber<T> assertValue(T value) {
    assertThat(takeValue()).isEqualTo(value);
    return this;
  }

  public void assertComplete() {
    Signal<T> signal = takeSignal();
    assertThat(signal.isOnComplete())
        .named("Expected onCompleted event but was %s", signal)
        .isTrue();
    assertNoEvents();
  }

  public void assertError(Throwable throwable) {
    assertThat(takeError()).isEqualTo(throwable);
  }

  public void assertError(Class<? extends Throwable> errorClass) {
    assertError(errorClass, null);
  }

  public void assertError(Class<? extends Throwable> errorClass, String message) {
    Throwable throwable = takeError();
    assertThat(throwable).isInstanceOf(errorClass);
    if (message != null) {
      assertThat(throwable).hasMessage(message);
    }
    assertNoEvents();
  }

  public void assertNoEvents() {
    assertThat(events).named("Unconsumed events found!").isEmpty();
  }

  public void requestMore(long amount) {
    s.request(amount);
  }

  public static final class Rule implements TestRule {
    final List<RecordingSubscriber<?>> subscribers = new ArrayList<>();

    public <T> RecordingSubscriber<T> create() {
      return createWithInitialRequest(Long.MAX_VALUE);
    }

    public <T> RecordingSubscriber<T> createWithInitialRequest(long initialRequest) {
      RecordingSubscriber<T> subscriber = new RecordingSubscriber<>(initialRequest);
      subscribers.add(subscriber);
      return subscriber;
    }

    @Override public Statement apply(final Statement base, Description description) {
      return new Statement() {
        @Override public void evaluate() throws Throwable {
          base.evaluate();
          for (RecordingSubscriber<?> subscriber : subscribers) {
            subscriber.assertNoEvents();
          }
        }
      };
    }
  }
}
