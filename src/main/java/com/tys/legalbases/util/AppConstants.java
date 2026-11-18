package com.tys.legalbases.util;

public class AppConstants {

    // Controller related constants
    public static final String API_NATIONAL_NORMS_BASE_PATH = "/api/national-norms";
    public static final String API_NATIONAL_NORMS_BY_CRITERIA = "/find-by-criteria";
    public static final String API_NATIONAL_NORMS_INGEST_PATH = "/ingest";
    public static final String API_NATIONAL_NORMS_RAG_PATH = "/rag";
    public static final String API_CASE_LAW_BASE_PATH = "/api/case-law";
    public static final String API_LEGAL_GRAPH_BASE_PATH = "/api/legal-graph";
    public static final String CROSS_ORIGIN_ALLOWED_ORIGINS = "*";
    public static final String CROSS_ORIGIN_ALLOWED_HEADERS = "*";

    // Entity related constants (table and column names, lengths, etc.)
    public static final String TABLE_NATIONAL_NORMS = "national_norms";
    public static final String TABLE_ARTICLES = "articles";
    public static final String COLUMN_NORM_ID = "norm_id";
    public static final String COLUMN_ARTICLE_NUMBER = "article_number";
    public static final String COLUMN_ARTICLE_SUFFIX = "article_suffix";

    public static final String COLUMN_ARTICLE_LABEL = "article_label";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_PUBLISHED_AT = "published_at";
    public static final String COLUMN_SOURCE_URL = "source_url";

    // Lengths
    public static final int TITLE_LENGTH = 255;
    public static final int TYPE_LENGTH = 50;
    // 10 no alcanzaba: el sufijo de un 2.2.8.18.7.1 son 11 caracteres.
    public static final int ARTICLE_SUFFIX_LENGTH = 60;

    public static final int ARTICLE_LABEL_LENGTH = 60;
    public static final int ARTICLE_TITLE_LENGTH = 500;

    // Column Definitions
    public static final String TEXT_COLUMN_DEFINITION = "TEXT";
    public static final String TIMESTAMP_WITH_TIME_ZONE_DEFINITION = "TIMESTAMP WITH TIME ZONE";
}
