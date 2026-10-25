// Anchos, altos y posiciones que salen de los datos: llegan en atributos data- y se aplican aquí,
// porque la política de contenido no deja escribirlos en un atributo style.

(function () {
    function aplicar(atributo, propiedad) {
        document.querySelectorAll('[data-' + atributo + ']').forEach(function (elemento) {
            const valor = parseFloat(elemento.getAttribute('data-' + atributo));
            if (!Number.isNaN(valor)) {
                elemento.style.setProperty(propiedad, valor + '%');
            }
        });
    }

    aplicar('ancho', 'width');
    aplicar('alto', 'height');
    aplicar('izquierda', 'left');
})();
