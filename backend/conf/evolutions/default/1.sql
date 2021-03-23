-- !Ups
CREATE TABLE public.access_tokens
(
    token TEXT PRIMARY KEY,
    bot_id TEXT NOT NULL
);

CREATE TABLE public.posts
(
    id BIGSERIAL PRIMARY KEY,
    url TEXT NOT NULL,
    title TEXT NOT NULL,
    author TEXT NOT NULL,
    posted_at BIGINT NOT NULL,
    created_at BIGINT NOT NULL
);

CREATE TABLE public.bots_posts
(
    id BIGSERIAL PRIMARY KEY,
    bot_id TEXT NOT NULL,
    post_id BIGINT NOT NULL REFERENCES posts(id)
);

-- !Downs
DROP TABLE public.access_tokens CASCADE;
DROP TABLE public.posts CASCADE;
DROP TABLE public.bots_posts CASCADE;