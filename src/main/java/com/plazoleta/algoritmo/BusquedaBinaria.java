package com.plazoleta.algoritmo;

import java.util.List;
import java.util.function.ToIntFunction;

/**
 * Algoritmo de búsqueda binaria sobre una lista ordenada: en cada paso descarta la mitad donde no está el objetivo.
 * Complejidad: O(log n) en tiempo.
 */
public final class BusquedaBinaria {

    private BusquedaBinaria() {
    }

    /**
     * Encuentra la primera posición cuyo elemento no es menor que el objetivo.
     * @param ordenada lista ordenada con el mismo criterio del comparador
     * @param comparaConObjetivo devuelve un valor negativo, cero o positivo si el elemento es menor, igual o mayor
     * @return posición encontrada, o el tamaño de la lista si todos los elementos son menores
     */
    public static <T> int limiteInferior(List<T> ordenada, ToIntFunction<? super T> comparaConObjetivo) {
        int inicio = 0;
        int fin = ordenada.size();
        while (inicio < fin) {
            int medio = inicio + (fin - inicio) / 2;
            if (comparaConObjetivo.applyAsInt(ordenada.get(medio)) < 0) {
                inicio = medio + 1;
            } else {
                fin = medio;
            }
        }
        return inicio;
    }
}
