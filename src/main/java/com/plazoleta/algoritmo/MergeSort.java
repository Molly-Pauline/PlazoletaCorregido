package com.plazoleta.algoritmo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Algoritmo de ordenamiento por mezcla (merge sort). Divide la lista en mitades, ordena cada una y las combina.
 * Complejidad: O(n log n) en tiempo y O(n) en memoria. Es estable: los elementos iguales conservan su orden.
 */
public final class MergeSort {

    private MergeSort() {
    }

    /** Devuelve una lista nueva y ordenada, sin modificar la original. */
    public static <T> List<T> ordenar(List<T> elementos, Comparator<? super T> comparador) {
        if (elementos.size() < 2) {
            return new ArrayList<>(elementos);
        }
        int mitad = elementos.size() / 2;
        List<T> izquierda = ordenar(elementos.subList(0, mitad), comparador);
        List<T> derecha = ordenar(elementos.subList(mitad, elementos.size()), comparador);
        return mezclar(izquierda, derecha, comparador);
    }

    /** Combina dos listas ya ordenadas tomando siempre el menor de los dos elementos al frente. */
    private static <T> List<T> mezclar(List<T> izquierda, List<T> derecha, Comparator<? super T> comparador) {
        List<T> resultado = new ArrayList<>(izquierda.size() + derecha.size());
        int i = 0;
        int d = 0;
        while (i < izquierda.size() && d < derecha.size()) {
            // Con <= se toma primero el de la izquierda en un empate, y eso mantiene la estabilidad.
            if (comparador.compare(izquierda.get(i), derecha.get(d)) <= 0) {
                resultado.add(izquierda.get(i++));
            } else {
                resultado.add(derecha.get(d++));
            }
        }
        resultado.addAll(izquierda.subList(i, izquierda.size()));
        resultado.addAll(derecha.subList(d, derecha.size()));
        return resultado;
    }
}
