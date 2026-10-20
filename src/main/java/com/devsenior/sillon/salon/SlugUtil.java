package com.devsenior.sillon.salon;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class SlugUtil {

    private static final Pattern NO_ALFANUMERICO = Pattern.compile("[^a-z0-9]+");

    public static String generarSlugBase(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "salon";
        }
        String normalizado = Normalizer.normalize(nombre, Normalizer.Form.NFD);
        String sinTildes = normalizado.replaceAll("\\p{M}", "").toLowerCase().trim();
        String slug = NO_ALFANUMERICO.matcher(sinTildes).replaceAll("-");
        slug = slug.replaceAll("^-+|-+$", "");
        return slug.isEmpty() ? "salon" : slug;
    }
}