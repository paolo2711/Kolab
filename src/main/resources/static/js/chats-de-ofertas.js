// en el detalle de una solicitud cada oferta tiene su chat plegado; abrir uno cierra el que estaba abierto

(function () {
    document.querySelectorAll('.k-chat-boton[data-hilo]').forEach(function (boton) {
        boton.addEventListener('click', function () {
            const hilo = document.getElementById('hilo-' + boton.dataset.hilo);
            const abierto = !hilo.hidden;
            document.querySelectorAll('.k-hilo').forEach(function (h) { h.hidden = true; });
            document.querySelectorAll('.k-chat-boton').forEach(function (b) { b.classList.remove('activo'); });
            if (!abierto) {
                hilo.hidden = false;
                boton.classList.add('activo');
            }
        });
    });
})();
