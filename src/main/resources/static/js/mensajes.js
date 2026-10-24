// Pantalla de mensajes: el buscador y los filtros trabajan sobre lo que ya está en la página, así
// que no piden nada al servidor. Al abrir una conversación el hilo se va al último mensaje.

(function () {
    const hilo = document.getElementById('hilo');
    if (hilo) {
        hilo.scrollTop = hilo.scrollHeight;
    }

    const buscador = document.getElementById('buscarCharla');
    const contenedor = document.getElementById('charlas');
    if (!buscador || !contenedor) {
        return;
    }

    const charlas = Array.from(contenedor.querySelectorAll('.k-msj-charla'));
    const sinResultados = document.getElementById('sinResultados');
    const botones = Array.from(document.querySelectorAll('.k-msj-filtro'));
    let soloSinLeer = false;

    function filtrar() {
        const texto = buscador.value.trim().toLowerCase();
        let visibles = 0;

        charlas.forEach(function (charla) {
            const coincide = texto === '' || charla.dataset.busca.includes(texto);
            const pasaElFiltro = !soloSinLeer || charla.dataset.sinLeer === 'true';
            const mostrar = coincide && pasaElFiltro;
            charla.hidden = !mostrar;
            if (mostrar) {
                visibles++;
            }
        });

        if (sinResultados) {
            sinResultados.hidden = visibles > 0 || charlas.length === 0;
        }
    }

    buscador.addEventListener('input', filtrar);

    botones.forEach(function (boton) {
        boton.addEventListener('click', function () {
            soloSinLeer = boton.dataset.filtro === 'sin-leer';
            botones.forEach(function (otro) {
                otro.setAttribute('aria-pressed', String(otro === boton));
            });
            filtrar();
        });
    });
})();
