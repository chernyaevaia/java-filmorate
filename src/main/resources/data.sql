INSERT INTO mpa (id, name) 
SELECT * FROM (VALUES 
    (1, 'G'), 
    (2, 'PG'), 
    (3, 'PG-13'), 
    (4, 'R'), 
    (5, 'NC-17')
) AS v(id, name)
WHERE NOT EXISTS (SELECT 1 FROM mpa WHERE mpa.id = v.id);

INSERT INTO genres (id, name) 
SELECT * FROM (VALUES 
    (1, 'Комедия'), 
    (2, 'Драма'), 
    (3, 'Мультфильм'), 
    (4, 'Триллер'), 
    (5, 'Документальный'), 
    (6, 'Боевик')
) AS v(id, name)
WHERE NOT EXISTS (SELECT 1 FROM genres WHERE genres.id = v.id);