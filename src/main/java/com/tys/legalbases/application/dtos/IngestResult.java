package com.tys.legalbases.application.dtos;

/** Explicit ingest outcome so callers can distinguish a create from an idempotent no-op. */
public record IngestResult(Outcome outcome) {

    public enum Outcome { CREATED, ALREADY_EXISTS }

    public static IngestResult created() {
        return new IngestResult(Outcome.CREATED);
    }

    public static IngestResult alreadyExists() {
        return new IngestResult(Outcome.ALREADY_EXISTS);
    }
}
