package com.pie.demo.manager;

public interface RecoringManaegerInterfaceOne {
    void onNext(String result, Boolean completed);

    void onError(String message);
}
