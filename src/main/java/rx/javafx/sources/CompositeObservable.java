/**
 * Copyright 2016 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rx.javafx.sources;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import rx.Observable;
import rx.observables.JavaFxObservable;
import java.util.HashSet;
import java.util.Arrays;

/**
 * A CompositeObservable can merge multiple Observables that can be added/removed at any time,
 * affecting all Subscribers regardless of when they subscribed. This is especially helpful for merging
 * multiple UI event sources.
 *
 * @param <T>
 */
public final class CompositeObservable<T> {

    private final ObservableSet<Observable<T>> sources;
    private final int initialCapacity;

    public CompositeObservable() {
        this(-1);
    }

    public CompositeObservable(int initialCapacity) {
        this.initialCapacity = initialCapacity;
        sources = FXCollections.synchronizedObservableSet(FXCollections.observableSet(new HashSet<>()));
    }

    public Observable<T> toObservable() {
        Observable<T> updatingSource = Observable.merge(
                Observable.from(sources).flatMap(obs -> obs.takeWhile(v -> sources.contains(obs))),
                JavaFxObservable.fromObservableSetAdds(sources).flatMap(obs -> obs.takeWhile(v -> sources.contains(obs)))
        );

        if (initialCapacity > 0) {
            return updatingSource.cacheWithInitialCapacity(initialCapacity);
        } else {
            return updatingSource;
        }
    }

    public void add(Observable<T> observable) {
        sources.add(observable);
    }
    public void addAll(Observable<T>... observables) {
        Arrays.stream(observables).forEach(this::add);
    }
    public void remove(Observable<T> observable) {
        sources.remove(observable);
    }
    public void removeAll(Observable<T>... observables) {
        Arrays.stream(observables).forEach(this::remove);
    }
    public void clear() {
        sources.clear();
    }

}