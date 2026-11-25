-- =====================================================================
-- RAG 知识库表结构（MySQL 8）
-- 设计要点：
--   1. 文档与切片分离，kb_chunk.embedding 为 JSON（向量模式才写）
--   2. 录入 embedding_model / embedding_dim，换模型时可整表重建
--   3. FULLTEXT 使用 ngram 分词器，支持中文关键词检索
-- =====================================================================
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `kb_document` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '文档ID',
    `title`        VARCHAR(200) NOT NULL COMMENT '文档标题',
    `source`       VARCHAR(255) NOT NULL COMMENT '来源文件名',
    `content_hash` CHAR(64)     NOT NULL COMMENT '内容 SHA-256，用于幂等导入',
    `content`      MEDIUMTEXT   NULL COMMENT '文档原文，用于重建索引',
    `chunk_count`  INT          NOT NULL DEFAULT 0 COMMENT '切片数',
    `create_time`  DATETIME     NULL,
    `update_time`  DATETIME     NULL,
    `flag`         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_kb_document_hash` (`content_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;

CREATE TABLE IF NOT EXISTS `kb_chunk` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '切片ID',
    `doc_id`          BIGINT       NOT NULL COMMENT '文档ID',
    `seq`             INT          NOT NULL COMMENT '切片序号（从1开始）',
    `content`         TEXT         NOT NULL COMMENT '切片正文',
    `embedding`       JSON         NULL COMMENT '向量（JSON 数组）',
    `embedding_model` VARCHAR(100) NULL COMMENT '向量模型名',
    `embedding_dim`   INT          NULL COMMENT '向量维度',
    `token_count`     INT          NULL COMMENT '粗略字数',
    `create_time`     DATETIME     NULL,
    PRIMARY KEY (`id`),
    KEY `ix_kb_chunk_doc` (`doc_id`),
    FULLTEXT KEY `ft_kb_chunk_content` (`content`) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;
