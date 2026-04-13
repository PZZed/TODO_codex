package com.todoapp.todo.shared.application;

/** Contrat générique pour un cas d usage applicatif. */
public interface UseCase<I, O> {
    O execute(I input);
}

