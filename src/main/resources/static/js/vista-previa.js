// vista previa de Publicar: se repinta mientras se escribe. lo que ya estaba escrito lo pinta el servidor

(function () {
    const f = document.getElementById('formSolicitud');
    if (!f) {
        return;
    }
    const titulo = f.querySelector('#titulo');
    const categoria = f.querySelector('#idCategoria');
    const modalidad = f.querySelector('#modalidad');
    const precio = f.querySelector('#precioPropuesto');

    function pintar() {
        const vpTitulo = document.getElementById('vpTitulo');
        vpTitulo.textContent = titulo.value.trim() || 'Tu título va aquí';
        vpTitulo.classList.toggle('k-vp-vacio', !titulo.value.trim());
        document.getElementById('vpCategoria').textContent =
            categoria.selectedIndex > 0 ? categoria.options[categoria.selectedIndex].text : 'Categoría';
        document.getElementById('vpModalidad').textContent = modalidad.options[modalidad.selectedIndex].text;
        document.getElementById('vpPrecio').textContent = precio.value ? Number(precio.value).toFixed(2) : '0.00';
    }

    [titulo, categoria, modalidad, precio].forEach(function (campo) {
        campo.addEventListener('input', pintar);
        campo.addEventListener('change', pintar);
    });
})();
