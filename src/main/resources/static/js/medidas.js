// anchos, posiciones y colores que salen de los datos: llegan en data- y se aplican aca
// porque la politica de contenido no deja escribirlos en un atributo style

(function () {
    function aplicar(atributo, propiedad) {
        document.querySelectorAll('[data-' + atributo + ']').forEach(function (elemento) {
            const valor = parseFloat(elemento.getAttribute('data-' + atributo));
            if (!Number.isNaN(valor)) {
                elemento.style.setProperty(propiedad, valor + '%');
            }
        });
    }

    // solo un #rrggbb, cualquier otra cosa se ignora
    document.querySelectorAll('[data-fondo]').forEach(function (elemento) {
        const color = elemento.getAttribute('data-fondo');
        if (/^#[0-9a-f]{6}$/i.test(color)) {
            elemento.style.setProperty('background-color', color);
        }
    });

    aplicar('ancho', 'width');
    aplicar('alto', 'height');
    aplicar('izquierda', 'left');
})();
