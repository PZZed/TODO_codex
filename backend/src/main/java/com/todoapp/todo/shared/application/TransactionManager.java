package com.todoapp.todo.shared.application;

/** Abstraction de gestion transactionnelle pour découpler l application du framework. */
public interface TransactionManager {
    <T> T inTransaction(SupplierCallback<T> callback);

    interface SupplierCallback<T> {
        T get();
    }
}

