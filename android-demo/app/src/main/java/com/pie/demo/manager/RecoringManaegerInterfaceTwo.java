package com.pie.demo.manager;

public interface RecoringManaegerInterfaceTwo {
    void onNext(String result, Boolean completed);

    void onError(String message);
}
