package com.plazoleta.algoritmo;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Pruebas del merge sort y de la búsqueda binaria. */
class AlgoritmosTest {

    @Test
    void mergeSortOrdenaSinModificarLaOriginal() {
        List<Integer> original = List.of(5, 1, 4, 2, 3);

        assertEquals(List.of(1, 2, 3, 4, 5), MergeSort.ordenar(original, Comparator.naturalOrder()));
        assertEquals(List.of(5, 1, 4, 2, 3), original);
    }

    @Test
    void mergeSortAtiendeListasVaciasYDeUnElemento() {
        assertEquals(List.of(), MergeSort.ordenar(List.<Integer>of(), Comparator.naturalOrder()));
        assertEquals(List.of(7), MergeSort.ordenar(List.of(7), Comparator.naturalOrder()));
    }

    @Test
    void mergeSortEsEstable() {
        List<String> palabras = List.of("bb", "aa", "c", "dd", "e");

        assertEquals(List.of("c", "e", "bb", "aa", "dd"),
                MergeSort.ordenar(palabras, Comparator.comparingInt(String::length)));
    }

    @Test
    void busquedaBinariaEncuentraLaPrimeraPosicionNoMenor() {
        List<Integer> numeros = List.of(1, 3, 3, 5);

        assertEquals(1, BusquedaBinaria.limiteInferior(numeros, n -> n.compareTo(3)));
        assertEquals(3, BusquedaBinaria.limiteInferior(numeros, n -> n.compareTo(4)));
        assertEquals(0, BusquedaBinaria.limiteInferior(numeros, n -> n.compareTo(0)));
        assertEquals(4, BusquedaBinaria.limiteInferior(numeros, n -> n.compareTo(9)));
        assertEquals(0, BusquedaBinaria.limiteInferior(List.<Integer>of(), n -> n.compareTo(9)));
    }
}
