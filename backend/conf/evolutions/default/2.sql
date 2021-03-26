-- !Ups
ALTER TABLE public.access_tokens ADD COLUMN team_id TEXT NOT NULL DEFAULT 'hoge';

ALTER TABLE public.access_tokens RENAME TO work_spaces;


-- !Downs
ALTER TABLE public.work_spaces RENAME TO access_tokens;
ALTER TABLE public.access_tokens DROP COLUMN team_id;
