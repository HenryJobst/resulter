SELECT p1.*
FROM public.person p1
         JOIN public.person p2
              ON p1.family_name = p2.family_name
                  AND p1.given_name = p2.given_name
                  AND (p1.birth_date IS NULL OR p2.birth_date IS NULL OR p1.birth_date = p2.birth_date)
                  AND (p1.gender IS NULL OR p2.gender IS NULL OR p1.gender = p2.gender)
WHERE p1.id > p2.id
order by family_name, given_name, id;

-- ignore gender
SELECT p1.*
FROM public.person p1
         JOIN public.person p2
              ON p1.family_name = p2.family_name
                  AND p1.given_name = p2.given_name
                  AND (p1.birth_date IS NULL OR p2.birth_date IS NULL OR p1.birth_date = p2.birth_date)
WHERE p1.id > p2.id
order by family_name, given_name, id;

-- ignore birth date
SELECT p1.*
FROM public.person p1
         JOIN public.person p2
              ON p1.family_name = p2.family_name
                  AND p1.given_name = p2.given_name
                  AND (p1.gender IS NULL OR p2.gender IS NULL OR p1.gender = p2.gender)
WHERE p1.id > p2.id
order by family_name, given_name, id;
