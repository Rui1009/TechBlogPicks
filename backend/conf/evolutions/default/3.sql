-- !Ups
ALTER TABLE public.posts ADD COLUMN testimonial TEXT;


-- !Downs
ALTER TABLE public.posts DROP COLUMN testimonial;