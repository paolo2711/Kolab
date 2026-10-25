// vista previa de Publicar y Editar: se repinta mientras se escribe. lo que ya estaba escrito lo pinta el servidor

(function () {
    const f = document.getElementById('formSolicitud');
    const previa = document.getElementById('vistaPrevia');
    if (!f || !previa) {
        return;
    }
    const titulo = f.querySelector('#titulo');
    const categoria = f.querySelector('#idCategoria');
    const modalidad = f.querySelector('#modalidad');
    const precio = f.querySelector('#precioPropuesto');
    const archivo = f.querySelector('#foto');
    const vpFoto = document.getElementById('vpFoto');
    const vpSinFoto = document.getElementById('vpSinFoto');
    let elegida = null;

    function pintar() {
        const vpTitulo = document.getElementById('vpTitulo');
        vpTitulo.textContent = titulo.value.trim() || 'Tu título va aquí';
        vpTitulo.classList.toggle('k-vp-vacio', !titulo.value.trim());
        document.getElementById('vpCategoria').textContent =
            categoria.selectedIndex > 0 ? categoria.options[categoria.selectedIndex].text : 'Categoría';
        document.getElementById('vpModalidad').textContent = modalidad.options[modalidad.selectedIndex].text;
        document.getElementById('vpPrecio').textContent = precio.value ? Number(precio.value).toFixed(2) : '0.00';
    }

    // la foto elegida va primero; si no hay, la que ya tenia la solicitud y si no, la de la categoria
    function pintarFoto() {
        const foto = elegida || previa.dataset.fotoPropia || categoria.options[categoria.selectedIndex].dataset.foto;
        vpFoto.hidden = !foto;
        vpSinFoto.hidden = !!foto;
        if (foto) {
            vpFoto.src = foto;
        } else {
            vpFoto.removeAttribute('src');
        }
    }

    // la imagen local se muestra con una direccion blob: que vive solo en esta pestaña
    function elegirFoto() {
        if (elegida) {
            URL.revokeObjectURL(elegida);
        }
        const elegido = archivo.files[0];
        elegida = elegido && /^image\/(jpeg|png)$/.test(elegido.type) ? URL.createObjectURL(elegido) : null;
        pintarFoto();
    }

    [titulo, categoria, modalidad, precio].forEach(function (campo) {
        campo.addEventListener('input', pintar);
        campo.addEventListener('change', pintar);
    });
    categoria.addEventListener('change', pintarFoto);
    if (archivo) {
        archivo.addEventListener('change', elegirFoto);
    }
})();
