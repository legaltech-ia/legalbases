package com.tys.legalbases.application.dtos;

/** Explicit ingest outcome so callers can distinguish a create from an idempotent no-op. */
public record IngestResult(Outcome outcome) {

    public enum Outcome { CREATED, ALREADY_EXISTS, ARTICLES_ADDED }

    public static IngestResult created() {
        return new IngestResult(Outcome.CREATED);
    }

    public static IngestResult alreadyExists() {
        return new IngestResult(Outcome.ALREADY_EXISTS);
    }

    /**
     * La norma ya estaba registrada pero SIN articulado, y esta ingesta le puso
     * el texto.
     *
     * Merece un resultado propio porque la diferencia importa: el snapshot de
     * SUIN-Juriscol dejo 89.365 normas como titulo sin articulos, y una norma
     * sin articulado no se vectoriza ni se puede citar. Devolver ALREADY_EXISTS
     * en ese caso —como se hacia— convertia la ingesta en un no-op silencioso
     * justo para las normas que hay que completar.
     */
    public static IngestResult articlesAdded() {
        return new IngestResult(Outcome.ARTICLES_ADDED);
    }
}
