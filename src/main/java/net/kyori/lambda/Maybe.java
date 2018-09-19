/*
 * This file is part of lambda, licensed under the MIT License.
 *
 * Copyright (c) 2018 KyoriPowered
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.kyori.lambda;

import net.kyori.lambda.collection.MoreIterators;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Something that may or may not contain a value.
 *
 * @param <T> the value type
 */
public interface Maybe<T> extends Iterable<T> {
  /**
   * A collector that accumulates zero or one elements into a {@code Maybe}.
   *
   * @see #collector()
   */
  Collector<?, ?, Maybe<?>> COLLECTOR = Collectors.collectingAndThen(
    Collectors.reducing((a, b) -> {
      throw new AmbiguousElementException();
    }),
    Maybe::from
  );

  /**
   * Returns an empty {@code Maybe}.
   *
   * @param <T> the type
   * @return an empty {@code Maybe}
   */
  @SuppressWarnings("unchecked")
  static <T> @NonNull Maybe<T> nothing() {
    return (Maybe<T>) Nothing.NOTHING;
  }

  /**
   * Returns a {@code Maybe} containing {@code value}.
   *
   * @param value the value
   * @param <T> the type
   * @return a {@code Maybe} with a value
   */
  static <T> @NonNull Maybe<T> just(final @NonNull T value) {
    return new Just<>(requireNonNull(value));
  }

  /**
   * Returns a {@code Maybe} containing {@code value} if it is non-{@code null}, otherwise returns an empty {@code Maybe}.
   *
   * @param value the value
   * @param <T> the value type
   * @return a {@code Maybe} with a value
   */
  static <T> @NonNull Maybe<T> maybe(final @Nullable T value) {
    return value == null ? nothing() : just(value);
  }

  /**
   * Converts an {@code Optional} into a {@code Maybe}.
   *
   * @param optional the optional
   * @param <T> the value type
   * @return a {@code Maybe}
   */
  @SuppressWarnings("OptionalIsPresent")
  static <T> @NonNull Maybe<T> from(final @NonNull Optional<T> optional) {
    return optional.isPresent() ? just(optional.get()) : nothing();
  }

  /**
   * Returns {@code true} if this {@code Maybe} is empty.
   *
   * @return {@code true} if this {@code Maybe} is empty
   */
  boolean isEmpty();

  /**
   * Returns {@code true} if this {@code Maybe} has a value.
   *
   * @return {@code true} if this {@code Maybe} has a value
   */
  boolean isPopulated();

  /**
   * Gets the value if present, otherwise throws.
   *
   * @return the value
   * @throws NoSuchElementException if no value is present
   */
  T get();

  /**
   * Gets the value if present, otherwise returns {@code other}.
   *
   * @param other the other value
   * @return the value if present, otherwise {@code other}
   */
  T getOrElse(final @Nullable T other);

  /**
   * Gets the value if present, otherwise returns a value supplied by {@code other}.
   *
   * @param other the other value supplier
   * @return the value if present, otherwise a value supplied by {@code other}
   */
  T getOrGet(final @NonNull Supplier<? extends T> other);

  /**
   * Gets the value if present, otherwise throws an exception of type {@code X} supplied by {@code supplier} if not.
   *
   * @param supplier the exception supplier
   * @param <X> the exception type
   * @return the value
   * @throws X if no value is present
   */
  <X extends Throwable> T getOrThrow(final @NonNull Supplier<X> supplier) throws X;

  /**
   * Returns a {@code Maybe} containing the value if it is present, otherwise returns {@code that}.
   *
   * @param that the other maybe
   * @return a {@code Maybe} containing the value if it is present, otherwise returns {@code that}
   */
  @NonNull Maybe<T> or(final @NonNull Maybe<? extends T> that);

  /**
   * Returns a {@code Maybe} containing the value if it is present, otherwise returns a {@code Maybe} supplied by {@code that}.
   *
   * @param that the other maybe supplier
   * @return a {@code Maybe} containing the value if it is present, otherwise returns a {@code Maybe} supplied by {@code that}
   */
  @NonNull Maybe<T> or(final @NonNull Supplier<? extends Maybe<? extends T>> that);

  /**
   * Returns a {@code Maybe} containing the value if it is present and matches the given predicate, otherwise returns an empty {@code Maybe}.
   *
   * @param predicate the predicate to apply to the value if it is present
   * @return a {@code Maybe} containing the value if the value is present and the value matches the given predicate, otherwise an empty {@code Maybe}
   */
  @NonNull Maybe<T> filter(final @NonNull Predicate<? super T> predicate);

  /**
   * Returns a {@code Maybe} containing the result of applying the given function to the value if it is present, otherwise returns an empty {@code Maybe}.
   *
   * @param function the function to apply to the value if it is present
   * @param <U> the new value type
   * @return a {@code Maybe} containing the result of applying the given function to the value if it is present, otherwise an empty {@code Maybe}
   */
  <U> @NonNull Maybe<U> map(final @NonNull Function<? super T, ? extends U> function);

  /**
   * Returns the result of applying the given function to the value if it is present, otherwise returns an empty {@code Maybe}.
   *
   * @param function the function to apply to the value if it is present
   * @param <U> the new value type
   * @return the result of applying the given function to the value if it is present, otherwise an empty {@code Maybe}
   */
  <U> @NonNull Maybe<U> flatMap(final @NonNull Function<? super T, ? extends Maybe<? extends U>> function);

  /**
   * Returns a {@code Stream} containing the value if it is present, otherwise an empty {@code Stream}.
   *
   * @return a {@code Stream} containing the value if it is present, otherwise an empty {@code Stream}
   */
  @NonNull Stream<T> stream();

  /**
   * Converts this {@code Maybe} into an {@code Optional}.
   *
   * @return an optional
   */
  @NonNull Optional<T> optional();

  /**
   * Returns a collector that accumulates zero or one elements into a {@code Maybe}.
   *
   * @param <T> the value type
   * @return a collector that accumulates zero or one elements into a {@code Maybe}
   */
  @SuppressWarnings("unchecked")
  static <T> @NonNull Collector<T, ?, Maybe<T>> collector() {
    return (Collector) COLLECTOR;
  }

  /**
   * Returns the first {@link #isPopulated() populated} {@code Maybe}, or an empty {@code Maybe}.
   *
   * @param maybes the possible options
   * @param <T> the value type
   * @return the first populated {@code Maybe}, or an empty {@code Maybe}
   */
  @SafeVarargs
  @SuppressWarnings("unchecked")
  static <T> @NonNull Maybe<T> first(final @NonNull Maybe<? extends T>... maybes) {
    for(final Maybe<? extends T> maybe : maybes) {
      if(maybe.isPopulated()) {
        return (Maybe<T>) maybe;
      }
    }
    return nothing();
  }

  /**
   * Returns the first {@link #isPopulated() populated} {@code Maybe}, or an empty {@code Maybe}.
   *
   * @param maybes the possible options
   * @param <T> the value type
   * @return the first populated {@code Maybe}, or an empty {@code Maybe}
   */
  @SuppressWarnings("unchecked")
  static <T> @NonNull Maybe<T> first(final @NonNull Iterable<Maybe<? extends T>> maybes) {
    for(final Maybe<? extends T> maybe : maybes) {
      if(maybe.isPopulated()) {
        return (Maybe<T>) maybe;
      }
    }
    return nothing();
  }

  /**
   * Nothing.
   *
   * @param <T> the value type
   */
  final class Nothing<T> implements Maybe<T> {
    /* package */ static final Maybe<Object> NOTHING = new Nothing<>();

    /* package */ Nothing() {
    }

    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public boolean isPopulated() {
      return false;
    }

    @Override
    public T get() {
      throw new NoSuchElementException();
    }

    @Override
    public T getOrElse(final @Nullable T other) {
      return other;
    }

    @Override
    public T getOrGet(final @NonNull Supplier<? extends T> other) {
      return other.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull Maybe<T> or(final @NonNull Maybe<? extends T> that) {
      return (Maybe<T>) that;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull Maybe<T> or(final @NonNull Supplier<? extends Maybe<? extends T>> that) {
      return (Maybe<T>) that.get();
    }

    @Override
    public <X extends Throwable> T getOrThrow(final @NonNull Supplier<X> supplier) throws X {
      throw supplier.get();
    }

    @Override
    public @NonNull Maybe<T> filter(final @NonNull Predicate<? super T> predicate) {
      return nothing();
    }

    @Override
    public <U> @NonNull Maybe<U> map(final @NonNull Function<? super T, ? extends U> function) {
      return nothing();
    }

    @Override
    public <U> @NonNull Maybe<U> flatMap(final @NonNull Function<? super T, ? extends Maybe<? extends U>> function) {
      return nothing();
    }

    @Override
    public void forEach(final @NonNull Consumer<? super T> action) {
      // noop
    }

    @Override
    public @NonNull Stream<T> stream() {
      return Stream.empty();
    }

    @Override
    public @NonNull Optional<T> optional() {
      return Optional.empty();
    }

    @Override
    public @NonNull Iterator<T> iterator() {
      return Collections.emptyIterator();
    }

    @Override
    public @NonNull String toString() {
      return "Maybe.nothing()";
    }

    @Override
    public boolean equals(final @Nullable Object other) {
      return other instanceof Nothing<?>;
    }

    @Override
    public int hashCode() {
      return 0;
    }
  }

  /**
   * A defined value.
   *
   * @param <T> the value type
   */
  final class Just<T> implements Maybe<T> {
    private final T value;

    /* package */ Just(final T value) {
      this.value = value;
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

    @Override
    public boolean isPopulated() {
      return true;
    }

    @Override
    public T get() {
      return this.value;
    }

    @Override
    public T getOrElse(final @Nullable T other) {
      return this.value;
    }

    @Override
    public T getOrGet(final @NonNull Supplier<? extends T> other) {
      return this.value;
    }

    @Override
    public @NonNull Maybe<T> or(final @NonNull Maybe<? extends T> that) {
      return this;
    }

    @Override
    public @NonNull Maybe<T> or(final @NonNull Supplier<? extends Maybe<? extends T>> that) {
      return this;
    }

    @Override
    public <X extends Throwable> T getOrThrow(final @NonNull Supplier<X> supplier) {
      return this.value;
    }

    @Override
    public @NonNull Maybe<T> filter(final @NonNull Predicate<? super T> predicate) {
      return predicate.test(this.value) ? this : nothing();
    }

    @Override
    public <U> @NonNull Maybe<U> map(final @NonNull Function<? super T, ? extends U> function) {
      return just(function.apply(this.value));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U> @NonNull Maybe<U> flatMap(final @NonNull Function<? super T, ? extends Maybe<? extends U>> function) {
      return (Maybe<U>) function.apply(this.value);
    }

    @Override
    public void forEach(final @NonNull Consumer<? super T> action) {
      action.accept(this.value);
    }

    @Override
    public @NonNull Stream<T> stream() {
      return Stream.of(this.value);
    }

    @Override
    public @NonNull Optional<T> optional() {
      return Optional.ofNullable(this.value);
    }

    @Override
    public @NonNull Iterator<T> iterator() {
      return MoreIterators.singleton(this.value);
    }

    @Override
    public @NonNull String toString() {
      return "Maybe.just(" + this.value + ')';
    }

    @Override
    public boolean equals(final @Nullable Object other) {
      if(this == other) return true;
      if(other == null || this.getClass() != other.getClass()) return false;
      final Just<?> just = (Just<?>) other;
      return Objects.equals(this.value, just.value);
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.value);
    }
  }
}